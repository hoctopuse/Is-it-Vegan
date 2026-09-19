package com.example.isitvegan

import android.content.Context
import org.json.JSONArray
import java.math.BigDecimal

data class AnalysisResult(
    val matched: List<Ingredient>,
    val unknown: List<String>,
    val stoppedAtNonVegetarian: Boolean = false,
    val crossContactWarnings: List<String> = emptyList(),
    val excludedNotes: List<String> = emptyList()
) {
    val veganAssessment: VeganAssessment
        get() = VerdictEngine.assessVeganCompatibility(matched, unknown)

    val veganBlockers: List<Ingredient>
        get() = matched.filter {
            it.status == VeganStatus.VEGETARIAN || it.status == VeganStatus.NON_VEGAN
        }

    val uncertainIngredients: List<Ingredient>
        get() = matched.filter { it.status == VeganStatus.UNCERTAIN }

    val vegetarianIngredients: List<Ingredient>
        get() = matched.filter { it.status == VeganStatus.VEGETARIAN }

    /** Verdict for the known composition after setting uncertain ingredients aside. */
    val verdictWithoutUncertain: AnalysisVerdict
        get() = VerdictEngine.evaluate(matched, unknown, excludeUncertain = true)

    val verdict: AnalysisVerdict
        get() = VerdictEngine.evaluate(matched, unknown)
}

data class TokenDiagnostic(
    val text: String,
    val order: Int,
    val depth: Int,
    val parentOrder: Int?,
    val kind: NodeKind,
    val nodeKind: IngredientNodeKind,
    val compositionAfterQuantity: Boolean,
    val quantityPercent: BigDecimal?,
    val functionalClass: String?,
    val childCount: Int,
    val matcherText: String?,
    val matchedIngredientIds: List<String>,
    val blockedIngredientIds: List<String>,
    val matchKind: MatchKind,
    val unknown: String?
)

data class AnalysisDiagnostics(
    val input: String,
    val languageSegmentation: LanguageSegmentation,
    val labelSections: LabelSections,
    val preprocessedInput: String,
    val ingredientTree: List<IngredientNode>,
    val tokens: List<TokenDiagnostic>,
    val result: AnalysisResult
) {
    val crossContactWarnings: List<String> get() = result.crossContactWarnings
    val excludedNotes: List<String> get() = result.excludedNotes
}

enum class AnalysisVerdict { VEGAN, VEGETARIAN, NON_VEGETARIAN, UNCERTAIN, INCONCLUSIVE }
enum class VeganAssessment { VEGAN, NOT_VEGAN, UNCERTAIN }

object VeganAnalyzer {
    private var ingredients: List<Ingredient> = emptyList()

    fun loadDatabase(context: Context) {
        val json = context.assets.open("ingredients.json")
            .bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        ingredients = buildList {
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val aliasesArray = item.getJSONArray("aliases")
                val aliases = buildList {
                    for (j in 0 until aliasesArray.length()) add(aliasesArray.getString(j))
                }
                add(Ingredient(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    eNumber = item.optString("eNumber").takeIf { it.isNotBlank() },
                    aliases = aliases,
                    status = VeganStatus.valueOf(item.getString("status")),
                    reason = item.getString("reason"),
                    source = item.optString("source").takeIf { it.isNotBlank() }
                ))
            }
        }
    }

    fun analyze(text: String): AnalysisResult = analyze(text, ingredients)

    fun analyzeWithDiagnostics(text: String): AnalysisDiagnostics =
        analyzeWithDiagnostics(text, ingredients)

    // Exposed for JVM tests: analysis never needs an Android context or a network connection.
    internal fun analyze(text: String, database: List<Ingredient>): AnalysisResult {
        return runAnalysis(text, database).result
    }

    internal fun analyzeWithDiagnostics(
        text: String,
        database: List<Ingredient>
    ): AnalysisDiagnostics = runAnalysis(text, database)

    private fun runAnalysis(text: String, database: List<Ingredient>): AnalysisDiagnostics {
        val languageSegmentation = LabelLanguageSegmenter.segment(text)
        val sections = selectSections(languageSegmentation)
        val preprocessed = LabelPreprocessor.preprocess(sections.analysisText)
        val ingredientTree = IngredientTreeParser.parse(preprocessed.compositionText)
        val tokens = IngredientTokenizer.flatten(ingredientTree)
        val matcher = IngredientMatcher(database)
        val found = linkedMapOf<String, Ingredient>()
        val unknown = linkedMapOf<String, String>()
        val tokenDiagnostics = mutableListOf<TokenDiagnostic>()
        val tokenByOrder = tokens.associateBy { it.order }
        for (token in tokens) {
            if (token.kind == NodeKind.SECTION_HEADING ||
                token.kind == NodeKind.COMPOSITE_INGREDIENT
            ) {
                tokenDiagnostics += TokenDiagnostic(
                    text = token.text,
                    order = token.order,
                    depth = token.depth,
                    parentOrder = token.parentOrder,
                    kind = token.kind,
                    nodeKind = IngredientNodeKind.COMPOSITE,
                    compositionAfterQuantity = token.compositionAfterQuantity,
                    quantityPercent = token.quantityPercent,
                    functionalClass = token.functionalClass,
                    childCount = token.childCount,
                    matcherText = null,
                    matchedIngredientIds = emptyList(),
                    blockedIngredientIds = emptyList(),
                    matchKind = MatchKind.NONE,
                    unknown = null
                )
                continue
            }
            val matcherText = contextualMatcherText(token, tokenByOrder)
            val match = matcher.match(token.copy(text = matcherText))
            match.ingredients.forEach { found[it.id] = it }
            val assessment = UnknownCollector.assess(match)
            val tokenUnknown = assessment.unknown
            tokenUnknown?.let { unknown.putIfAbsent(TextNormalizer.normalize(it), it) }
            tokenDiagnostics += TokenDiagnostic(
                text = token.text,
                order = token.order,
                depth = token.depth,
                parentOrder = token.parentOrder,
                kind = token.kind,
                nodeKind = if (token.kind == NodeKind.ADDITIVE) {
                    IngredientNodeKind.ADDITIVE
                } else {
                    IngredientNodeKind.LEAF
                },
                compositionAfterQuantity = token.compositionAfterQuantity,
                quantityPercent = token.quantityPercent,
                functionalClass = token.functionalClass,
                childCount = token.childCount,
                matcherText = matcherText,
                matchedIngredientIds = match.ingredients.map { it.id },
                blockedIngredientIds = match.blockedIngredientIds,
                matchKind = assessment.matchKind,
                unknown = tokenUnknown
            )
        }
        return AnalysisDiagnostics(
            input = text,
            languageSegmentation = languageSegmentation,
            labelSections = sections,
            preprocessedInput = preprocessed.compositionText,
            ingredientTree = ingredientTree,
            tokens = tokenDiagnostics,
            result = AnalysisResult(
                found.values.toList(), unknown.values.toList(), false,
                (preprocessed.crossContactWarnings + listOfNotNull(sections.tracesText)).distinct(),
                preprocessed.excludedNotes
            )
        )
    }

    private fun selectSections(segmentation: LanguageSegmentation): LabelSections {
        val candidates = segmentation.blocks.map { LabelSectionExtractor.extract(it) }
        val priority = listOf(
            LabelLanguage.FRENCH,
            LabelLanguage.DUTCH,
            LabelLanguage.ENGLISH,
            LabelLanguage.GERMAN,
            LabelLanguage.SPANISH
        )
        return priority.firstNotNullOfOrNull { language ->
            candidates.firstOrNull { it.language == language && !it.ingredientsText.isNullOrBlank() }
        } ?: candidates.firstOrNull { it.hasIngredientHeading }
            ?: candidates.maxByOrNull { it.ingredientsText?.length ?: 0 }
            ?: LabelSectionExtractor.extract(LabelLanguage.UNKNOWN, segmentation.originalText)
    }

    private fun contextualMatcherText(
        token: IngredientToken,
        tokenByOrder: Map<Int, IngredientToken>
    ): String {
        val parent = token.parentOrder?.let(tokenByOrder::get) ?: return token.text
        val normalizedParent = TextNormalizer.normalize(parent.text)
        val normalizedChild = TextNormalizer.normalize(token.text)
        val isVegetableOilGroup = normalizedParent.matches(
            Regex("^huiles? vegetales? en proportion variable$")
        )
        val isSimpleChild = normalizedChild.matches(Regex("[a-z]+"))
        return if (isVegetableOilGroup && isSimpleChild) "huile de ${token.text}" else token.text
    }

}

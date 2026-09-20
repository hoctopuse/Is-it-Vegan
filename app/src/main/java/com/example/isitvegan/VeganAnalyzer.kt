package com.example.isitvegan

import android.content.Context
import org.json.JSONArray
import java.math.BigDecimal

data class AnalysisResult(
    val matched: List<Ingredient>,
    val unknown: List<String>,
    val availability: AnalysisAvailability = AnalysisAvailability.INGREDIENT_LIST_ANALYZED,
    val declaredPresenceIngredientIds: List<String> = emptyList(),
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
    val verdictWithoutUncertain: AnalysisVerdict?
        get() = if (availability == AnalysisAvailability.NO_INGREDIENT_LIST) null
        else VerdictEngine.evaluate(matched, unknown, excludeUncertain = true)

    val verdict: AnalysisVerdict?
        get() = if (availability == AnalysisAvailability.NO_INGREDIENT_LIST) null
        else VerdictEngine.evaluate(matched, unknown)
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
    val functionalClassCanonical: FunctionalClass?,
    val isNano: Boolean,
    val nanoText: String?,
    val variableProportions: Boolean,
    val variableProportionsText: String?,
    val hasAlternatives: Boolean,
    val alternativesText: String?,
    val sourceClaim: SourceClaim,
    val sourceClaimText: String?,
    val baseStatuses: List<VeganStatus>,
    val effectiveStatuses: List<VeganStatus>,
    val originResolution: String?,
    val isDeclaredPresence: Boolean,
    val childCount: Int,
    val matcherText: String?,
    val matchedIngredientIds: List<String>,
    val blockedIngredientIds: List<String>,
    val matchKind: MatchKind,
    val unknown: String?
)

data class AnalysisDiagnostics(
    val input: String,
    val inputMode: InputMode,
    val availabilityReason: String?,
    val usedManualFallback: Boolean,
    val languageSegmentation: LanguageSegmentation,
    val labelSections: LabelSections,
    val preprocessedInput: String,
    val declaredPresenceText: String?,
    val ingredientTree: List<IngredientNode>,
    val tokens: List<TokenDiagnostic>,
    val result: AnalysisResult
) {
    val crossContactWarnings: List<String> get() = result.crossContactWarnings
    val excludedNotes: List<String> get() = result.excludedNotes
}

enum class AnalysisVerdict { VEGAN, VEGETARIAN, NON_VEGETARIAN, UNCERTAIN, INCONCLUSIVE }
enum class VeganAssessment { VEGAN, NOT_VEGAN, UNCERTAIN }
enum class InputMode { MANUAL_INGREDIENT_LIST, FULL_LABEL, OCR_LABEL }
enum class AnalysisAvailability { INGREDIENT_LIST_ANALYZED, NO_INGREDIENT_LIST }

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

    fun analyze(
        text: String,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    ): AnalysisResult = analyze(text, ingredients, inputMode)

    fun analyzeWithDiagnostics(
        text: String,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    ): AnalysisDiagnostics = analyzeWithDiagnostics(text, ingredients, inputMode)

    // Exposed for JVM tests: analysis never needs an Android context or a network connection.
    internal fun analyze(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    ): AnalysisResult {
        return runAnalysis(text, database, inputMode).result
    }

    internal fun analyzeWithDiagnostics(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    ): AnalysisDiagnostics = runAnalysis(text, database, inputMode)

    private fun runAnalysis(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode
    ): AnalysisDiagnostics {
        val languageSegmentation = LabelLanguageSegmenter.segment(text)
        val sections = selectSections(languageSegmentation, inputMode)
        val explicitList = sections.hasIngredientHeading && !sections.ingredientsText.isNullOrBlank()
        val manualList = inputMode == InputMode.MANUAL_INGREDIENT_LIST &&
            !sections.ingredientsText.isNullOrBlank()
        val canParseIngredientText = explicitList || manualList
        val requestedManualFallback = manualList && !sections.hasIngredientHeading
        val ingredientInput = when {
            !canParseIngredientText -> ""
            requestedManualFallback -> sections.rawText
            else -> sections.ingredientsText.orEmpty()
        }
        val preprocessed = LabelPreprocessor.preprocess(ingredientInput)
        val presencePreprocessed = LabelPreprocessor.preprocess(sections.declaredContainsText.orEmpty())
        val ingredientTree = IngredientTreeParser.parse(preprocessed.compositionText)
        val availability = if (canParseIngredientText && ingredientTree.isNotEmpty()) {
            AnalysisAvailability.INGREDIENT_LIST_ANALYZED
        } else {
            AnalysisAvailability.NO_INGREDIENT_LIST
        }
        val usedManualFallback = requestedManualFallback &&
            availability == AnalysisAvailability.INGREDIENT_LIST_ANALYZED
        val availabilityReason = if (availability == AnalysisAvailability.NO_INGREDIENT_LIST) {
            if (inputMode == InputMode.MANUAL_INGREDIENT_LIST) {
                "Aucune liste manuelle exploitable n’a été fournie."
            } else {
                "Aucune section d’ingrédients reconnue dans l’étiquette."
            }
        } else null
        val ingredientTokens = IngredientTokenizer.flatten(ingredientTree)
        val presenceTokens = IngredientTokenizer.flatten(
            IngredientTreeParser.parse(presencePreprocessed.compositionText)
        ).map { token ->
            token.copy(
                order = token.order + ingredientTokens.size,
                parentOrder = token.parentOrder?.plus(ingredientTokens.size)
            )
        }
        val tokens = ingredientTokens + presenceTokens
        val presenceOrders = presenceTokens.mapTo(hashSetOf()) { it.order }
        val matcher = IngredientMatcher(database)
        val found = linkedMapOf<String, Ingredient>()
        val declaredPresence = linkedSetOf<String>()
        val unknown = linkedMapOf<String, String>()
        val tokenDiagnostics = mutableListOf<TokenDiagnostic>()
        val tokenByOrder = tokens.associateBy { it.order }
        for (token in tokens) {
            val isDeclaredPresence = token.order in presenceOrders
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
                    functionalClassCanonical = token.functionalClassCanonical,
                    isNano = token.isNano,
                    nanoText = token.nanoText,
                    variableProportions = token.variableProportions,
                    variableProportionsText = token.variableProportionsText,
                    hasAlternatives = token.hasAlternatives,
                    alternativesText = token.alternativesText,
                    sourceClaim = token.sourceClaim,
                    sourceClaimText = token.sourceClaimText,
                    baseStatuses = emptyList(),
                    effectiveStatuses = emptyList(),
                    originResolution = null,
                    isDeclaredPresence = isDeclaredPresence,
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
            val resolutions = match.ingredients.map {
                it to SourceClaimResolver.resolve(it, token.sourceClaim)
            }
            val effectiveIngredients = resolutions.map { (ingredient, resolution) ->
                ingredient.copy(
                    status = resolution.effectiveStatus,
                    reason = resolution.explanation ?: ingredient.reason
                )
            }
            effectiveIngredients.forEach { ingredient ->
                val previous = found[ingredient.id]
                if (previous == null || statusPriority(ingredient.status) > statusPriority(previous.status)) {
                    found[ingredient.id] = ingredient
                }
                if (isDeclaredPresence) declaredPresence += ingredient.id
            }
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
                functionalClassCanonical = token.functionalClassCanonical,
                isNano = token.isNano,
                nanoText = token.nanoText,
                variableProportions = token.variableProportions,
                variableProportionsText = token.variableProportionsText,
                hasAlternatives = token.hasAlternatives,
                alternativesText = token.alternativesText,
                sourceClaim = token.sourceClaim,
                sourceClaimText = token.sourceClaimText,
                baseStatuses = resolutions.map { it.second.baseStatus },
                effectiveStatuses = resolutions.map { it.second.effectiveStatus },
                originResolution = resolutions.mapNotNull { it.second.explanation }
                    .distinct().joinToString().takeIf(String::isNotBlank),
                isDeclaredPresence = isDeclaredPresence,
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
            inputMode = inputMode,
            availabilityReason = availabilityReason,
            usedManualFallback = usedManualFallback,
            languageSegmentation = languageSegmentation,
            labelSections = sections,
            preprocessedInput = preprocessed.compositionText,
            declaredPresenceText = presencePreprocessed.compositionText.takeIf(String::isNotBlank),
            ingredientTree = ingredientTree,
            tokens = tokenDiagnostics,
            result = AnalysisResult(
                matched = found.values.toList(),
                unknown = unknown.values.toList(),
                availability = availability,
                declaredPresenceIngredientIds = declaredPresence.toList(),
                stoppedAtNonVegetarian = false,
                crossContactWarnings = (
                    preprocessed.crossContactWarnings + presencePreprocessed.crossContactWarnings +
                        if (usedManualFallback) emptyList() else listOfNotNull(sections.tracesText)
                    ).distinct(),
                excludedNotes = (preprocessed.excludedNotes + presencePreprocessed.excludedNotes).distinct()
            )
        )
    }

    private fun statusPriority(status: VeganStatus): Int = when (status) {
        VeganStatus.VEGAN -> 0
        VeganStatus.UNCERTAIN -> 1
        VeganStatus.VEGETARIAN -> 2
        VeganStatus.NON_VEGAN -> 3
    }

    private fun selectSections(
        segmentation: LanguageSegmentation,
        inputMode: InputMode
    ): LabelSections {
        val candidates = segmentation.blocks.map { LabelSectionExtractor.extract(it) }
        val priority = listOf(
            LabelLanguage.FRENCH,
            LabelLanguage.DUTCH,
            LabelLanguage.ENGLISH,
            LabelLanguage.GERMAN,
            LabelLanguage.SPANISH
        )
        val titled = candidates.filter { it.hasIngredientHeading }
        val titledSelection = priority.firstNotNullOfOrNull { language ->
            titled.firstOrNull { it.language == language }
        }
        if (titledSelection != null) return titledSelection
        val segmentedSelection = candidates.firstOrNull {
            it.language == segmentation.selectedLanguage && it.rawText == segmentation.selectedText
        }
        if (inputMode == InputMode.MANUAL_INGREDIENT_LIST) {
            val manualSelection = priority.firstNotNullOfOrNull { language ->
                candidates.firstOrNull {
                    it.language == language && !it.ingredientsText.isNullOrBlank()
                }
            } ?: candidates.maxByOrNull { it.ingredientsText?.length ?: 0 }
            if (manualSelection != null) return manualSelection
        }
        return segmentedSelection
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
            Regex("^huiles? vegetales?$")
        )
        val isSimpleChild = normalizedChild.matches(Regex("[a-z]+"))
        return if (isVegetableOilGroup && isSimpleChild) "huile de ${token.text}" else token.text
    }

}

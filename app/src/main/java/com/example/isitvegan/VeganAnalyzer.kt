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
    val excludedNotes: List<String> = emptyList(),
    val originNonVeganIngredientIds: List<String> = emptyList()
) {
    val veganAssessment: VeganAssessment
        get() = if (originNonVeganIngredientIds.isNotEmpty()) VeganAssessment.NOT_VEGAN
        else VerdictEngine.assessVeganCompatibility(matched, unknown)

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
    val originRuleId: String?,
    val originOutcomeId: String?,
    val originQualifierText: String?,
    val baseStatuses: List<VeganStatus>,
    val effectiveStatuses: List<VeganStatus>,
    val originResolution: String?,
    val isDeclaredPresence: Boolean,
    val childCount: Int,
    val matcherText: String?,
    val correctedText: String?,
    val multilingualAlias: String?,
    val canonicalConceptId: String?,
    val canonicalConceptAvailable: Boolean?,
    val matchingLanguage: LabelLanguage?,
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
    val ocrCorrections: List<String>,
    val parenthesisStructure: ParenthesisStructure,
    val declaredPresenceText: String?,
    val ingredientTree: List<IngredientNode>,
    val tokens: List<TokenDiagnostic>,
    val originRuleErrors: List<String>,
    val result: AnalysisResult
) {
    val crossContactWarnings: List<String> get() = result.crossContactWarnings
    val excludedNotes: List<String> get() = result.excludedNotes
}

data class ParenthesisStructure(
    val balanced: Boolean,
    val missingClosings: Int,
    val unexpectedClosings: Int,
    val maximumDepth: Int,
    val recoveryApplied: Boolean
)

enum class AnalysisVerdict { VEGAN, VEGETARIAN, NON_VEGETARIAN, UNCERTAIN, INCONCLUSIVE }
enum class VeganAssessment { VEGAN, NOT_VEGAN, UNCERTAIN }
enum class InputMode { MANUAL_INGREDIENT_LIST, FULL_LABEL, OCR_LABEL }
enum class AnalysisAvailability { INGREDIENT_LIST_ANALYZED, NO_INGREDIENT_LIST }

object VeganAnalyzer {
    private var ingredients: List<Ingredient> = emptyList()
    private var originRules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty()
    private var originRuleErrors: List<String> = emptyList()
    private var multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    @Volatile private var databaseLoaded = false

    fun isDatabaseLoaded(): Boolean = databaseLoaded

    @Synchronized
    fun loadDatabase(context: Context) {
        if (databaseLoaded) return
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
        val originJson = context.assets.open("origin_qualifier_rules.json")
            .bufferedReader().use { it.readText() }
        val loadedRules = OriginQualifierRuleSet.load(originJson)
        originRules = loadedRules.rules
        originRuleErrors = loadedRules.errors
        multilingualLexicon = MultilingualIngredientLexicon.load(
            context.assets.open("ingredient_aliases_multilingual.json")
                .bufferedReader().use { it.readText() }
        )
        databaseLoaded = true
    }

    fun analyze(
        text: String,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    ): AnalysisResult = runAnalysis(text, ingredients, inputMode, originRules, originRuleErrors, multilingualLexicon = multilingualLexicon).result

    fun analyzeWithDiagnostics(
        text: String,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        preferredLanguage: UiLanguage = UiLanguage.FR,
        selectedBlockId: String? = null
    ): AnalysisDiagnostics = runAnalysis(
        text, ingredients, inputMode, originRules, originRuleErrors, preferredLanguage, selectedBlockId, multilingualLexicon
    )

    // Exposed for JVM tests: analysis never needs an Android context or a network connection.
    internal fun analyze(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        rules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty(),
        ruleErrors: List<String> = emptyList(),
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisResult {
        return runAnalysis(text, database, inputMode, rules, ruleErrors, multilingualLexicon = multilingualLexicon).result
    }

    internal fun analyzeWithDiagnostics(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        rules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty(),
        ruleErrors: List<String> = emptyList(),
        preferredLanguage: UiLanguage = UiLanguage.FR,
        selectedBlockId: String? = null,
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisDiagnostics = runAnalysis(
        text, database, inputMode, rules, ruleErrors, preferredLanguage, selectedBlockId, multilingualLexicon
    )

    private fun runAnalysis(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode,
        rules: OriginQualifierRuleSet,
        ruleErrors: List<String>,
        preferredLanguage: UiLanguage = UiLanguage.FR,
        selectedBlockId: String? = null,
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisDiagnostics {
        val detectedSegmentation = LabelLanguageSegmenter.segment(text, preferredLanguage)
        val languageSegmentation = detectedSegmentation.withSelectedBlockId(selectedBlockId)
        val sections = selectSections(languageSegmentation, inputMode, preferredLanguage)
        val explicitList = sections.hasIngredientHeading && !sections.ingredientsText.isNullOrBlank()
        val manualList = inputMode == InputMode.MANUAL_INGREDIENT_LIST &&
            !sections.ingredientsText.isNullOrBlank()
        val canParseIngredientText = explicitList || manualList
        val requestedManualFallback = manualList && !sections.hasIngredientHeading
        val ingredientInput = when {
            !canParseIngredientText -> ""
            requestedManualFallback -> sections.rawText
            else -> sections.ingredientsText
        }
        val preprocessed = LabelPreprocessor.preprocess(ingredientInput)
        val presencePreprocessed = LabelPreprocessor.preprocess(sections.declaredContainsText.orEmpty())
        val parenthesisStructure = IngredientTreeParser.inspectParentheses(preprocessed.compositionText)
        val ingredientTree = IngredientTreeParser.parse(preprocessed.compositionText, rules)
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
            IngredientTreeParser.parse(presencePreprocessed.compositionText, rules)
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
        val originNonVegan = linkedSetOf<String>()
        val unknown = linkedMapOf<String, String>()
        val tokenDiagnostics = mutableListOf<TokenDiagnostic>()
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
                    originRuleId = token.originQualification?.ruleId,
                    originOutcomeId = token.originQualification?.outcomeId,
                    originQualifierText = token.originQualification?.detectedPhrase,
                    baseStatuses = emptyList(),
                    effectiveStatuses = emptyList(),
                    originResolution = null,
                    isDeclaredPresence = isDeclaredPresence,
                    childCount = token.childCount,
                    matcherText = null,
                    correctedText = null,
                    multilingualAlias = null,
                    canonicalConceptId = null,
                    canonicalConceptAvailable = null,
                    matchingLanguage = null,
                    matchedIngredientIds = emptyList(),
                    blockedIngredientIds = emptyList(),
                    matchKind = MatchKind.NONE,
                    unknown = null
                )
                continue
            }
            // Preserve protected structural expressions before applying a language alias. Their
            // parenthesis is metadata, not a multilingual ingredient designation.
            val originalProtectedExpression = rules.protectedExpression(token.text)
            val lexical = multilingualLexicon.resolve(
                originalProtectedExpression?.matcherAlias ?: token.text,
                sections.language,
                database
            )
            val canonicalIngredient = lexical.canonicalId?.let { id -> database.firstOrNull { it.id == id } }
            val canonicalText = canonicalIngredient?.let { ingredient ->
                val suffix = lexical.correctedText.indexOf('(').takeIf { it >= 0 }
                    ?.let { lexical.correctedText.substring(it) }.orEmpty()
                ingredient.name + suffix
            } ?: lexical.correctedText
            val matcherText = OcrIngredientNormalizer.forMatching(canonicalText)
            val normalizedOrigin = rules.extractAttached(matcherText)
            val matchingToken = token.copy(
                text = matcherText,
                originQualification = normalizedOrigin.qualification ?: token.originQualification
            )
            val protectedExpression = originalProtectedExpression ?: rules.protectedExpression(matcherText)
            val rawMatch = matcher.match(matchingToken.copy(text = protectedExpression?.matcherAlias ?: matcherText))
            val match = validateProtectedExpression(rawMatch, protectedExpression)
            val resolutions = match.ingredients.map {
                it to rules.resolve(it, matchingToken.originQualification)
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
            resolutions.filter { it.second.explicitlyNotVegan }
                .forEach { originNonVegan += it.first.id }
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
                originRuleId = matchingToken.originQualification?.ruleId,
                originOutcomeId = matchingToken.originQualification?.outcomeId,
                originQualifierText = matchingToken.originQualification?.detectedPhrase,
                baseStatuses = resolutions.map { it.second.baseStatus },
                effectiveStatuses = resolutions.map { it.second.effectiveStatus },
                originResolution = resolutions.mapNotNull { it.second.explanation }
                    .distinct().joinToString().takeIf(String::isNotBlank),
                isDeclaredPresence = isDeclaredPresence,
                childCount = token.childCount,
                matcherText = matcherText,
                correctedText = lexical.correctedText.takeIf { it != token.text },
                multilingualAlias = lexical.alias,
                canonicalConceptId = lexical.canonicalId,
                canonicalConceptAvailable = lexical.canonicalAvailable,
                matchingLanguage = lexical.language.takeIf { lexical.alias != null },
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
            ocrCorrections = preprocessed.ocrCorrections,
            parenthesisStructure = parenthesisStructure,
            declaredPresenceText = presencePreprocessed.compositionText.takeIf(String::isNotBlank),
            ingredientTree = ingredientTree,
            tokens = tokenDiagnostics,
            originRuleErrors = ruleErrors,
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
                excludedNotes = (preprocessed.excludedNotes + presencePreprocessed.excludedNotes).distinct(),
                originNonVeganIngredientIds = originNonVegan.toList()
            )
        )
    }

    private fun LanguageSegmentation.withSelectedBlockId(sourceBlockId: String?): LanguageSegmentation {
        if (sourceBlockId == null || usedFallback || selectedBlockId == null) return this
        val currentSelectedId = selectedBlockId
        return copy(
            blocks = blocks.map { block ->
                if (block.id == currentSelectedId) block.copy(id = sourceBlockId) else block
            },
            selectedBlockId = sourceBlockId
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
        inputMode: InputMode,
        preferredLanguage: UiLanguage = UiLanguage.FR
    ): LabelSections {
        val candidates = segmentation.blocks.map { LabelSectionExtractor.extract(it) }
        val segmentedSelection = candidates.firstOrNull { it.selectedBlockId == segmentation.selectedBlockId }
        if (inputMode != InputMode.MANUAL_INGREDIENT_LIST) {
            return segmentedSelection
                ?: LabelSectionExtractor.extract(LabelLanguage.UNKNOWN, segmentation.originalText)
        }
        val priority = (when (preferredLanguage) {
            UiLanguage.FR -> listOf(LabelLanguage.FRENCH, LabelLanguage.ENGLISH, LabelLanguage.DUTCH)
            UiLanguage.EN -> listOf(LabelLanguage.ENGLISH, LabelLanguage.FRENCH, LabelLanguage.DUTCH)
            UiLanguage.NL -> listOf(LabelLanguage.DUTCH, LabelLanguage.ENGLISH, LabelLanguage.FRENCH)
        } + listOf(
            LabelLanguage.GERMAN,
            LabelLanguage.SPANISH
        ))
        val manualSelection = priority.firstNotNullOfOrNull { language ->
            candidates.firstOrNull {
                it.language == language && !it.ingredientsText.isNullOrBlank()
            }
        } ?: candidates.maxByOrNull { it.ingredientsText?.length ?: 0 }
        if (manualSelection != null) return manualSelection
        return segmentedSelection
            ?: LabelSectionExtractor.extract(LabelLanguage.UNKNOWN, segmentation.originalText)
    }

    private fun validateProtectedExpression(
        match: IngredientMatch,
        expression: ProtectedExpressionMatch?
    ): IngredientMatch {
        if (expression == null) return match
        val configuredIngredient = match.ingredients.singleOrNull {
            it.id == expression.ingredientId && it.status == expression.knownStatus
        } ?: return IngredientMatch(
            token = match.token,
            ingredients = emptyList(),
            residualNormalized = TextNormalizer.normalize(match.token.text),
            resolution = MatchResolution.NONE
        )
        return match.copy(ingredients = listOf(configuredIngredient))
    }

}

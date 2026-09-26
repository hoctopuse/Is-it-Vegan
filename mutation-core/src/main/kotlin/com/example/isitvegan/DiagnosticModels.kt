package com.example.isitvegan

/** Deterministic warnings derived from the submitted text and existing parser metadata. */
enum class DiagnosticInputWarning {
    EMPTY_INPUT,
    MANIFESTLY_TRUNCATED_BLOCK,
    UNBALANCED_STRUCTURE
}

enum class IgnoredSectionReason {
    OUTSIDE_INGREDIENT_COMPOSITION
}

data class IgnoredSectionDiagnostic(
    val text: String,
    val reason: IgnoredSectionReason
)

/** Stable groups used by tests and renderers without reparsing the text report. */
data class IngredientDiagnosticGroups(
    val veganIngredientIds: List<String>,
    val vegetarianIngredientIds: List<String>,
    val nonVegetarianIngredientIds: List<String>,
    val uncertainIngredientIds: List<String>,
    val unknownIngredients: List<String>
)

data class VerdictIngredientReference(
    /** Stable identity of this analyzed occurrence, independent of its text and path. */
    val occurrenceId: String,
    val ingredientId: String,
    val displayName: String,
    val path: List<String>,
    val status: VeganStatus,
    val reason: String
)

enum class ConditionalVerdictReason {
    UNCERTAIN_INGREDIENTS_EXCLUDED,
    KNOWN_NON_VEGETARIAN_INGREDIENT_REMAINS,
    UNKNOWN_INGREDIENT_REMAINS,
    NO_RELIABLE_ANALYSIS,
    NO_UNCERTAIN_INGREDIENT
}

/** Structured explanation derived from the completed analysis; it never changes the main verdict. */
data class VerdictExplanation(
    val mainVerdict: AnalysisVerdict?,
    val mainVeganAssessment: VeganAssessment,
    val knownBlockingIngredients: List<VerdictIngredientReference>,
    val uncertainIngredients: List<VerdictIngredientReference>,
    val conditionalVerdict: AnalysisVerdict?,
    val conditionalReason: ConditionalVerdictReason,
    val vegetarianStatus: AnalysisVerdict?,
    val tracesExcludedFromConditionalVerdict: Boolean
)

enum class DecisionReason {
    NO_INGREDIENT_LIST,
    EXPLICIT_NON_VEGAN_ORIGIN,
    KNOWN_VEGAN_BLOCKER,
    UNCERTAIN_INGREDIENT,
    UNKNOWN_INGREDIENT,
    NO_RECOGNIZED_INGREDIENT,
    ALL_RECOGNIZED_INGREDIENTS_VEGAN
}

/** Explanation of the already-computed result. It never participates in verdict calculation. */
data class DecisionDiagnostic(
    val veganAssessment: VeganAssessment,
    val detailedVerdict: AnalysisVerdict?,
    val vegetarianVerdictWithoutUncertain: AnalysisVerdict?,
    val reason: DecisionReason,
    val responsibleIngredientIds: List<String>,
    val unknownIngredients: List<String>,
    val unknownPreventsVegan: Boolean,
    val tracesExcludedFromVerdict: Boolean
)

internal fun AnalysisDiagnostics.deriveInputWarnings(): List<DiagnosticInputWarning> = buildList {
    if (input.isBlank()) add(DiagnosticInputWarning.EMPTY_INPUT)
    if (languageSegmentation.blocks.any { it.manifestlyTruncated }) {
        add(DiagnosticInputWarning.MANIFESTLY_TRUNCATED_BLOCK)
    }
    if (!parenthesisStructure.balanced) add(DiagnosticInputWarning.UNBALANCED_STRUCTURE)
}

internal fun AnalysisResult.toIngredientDiagnosticGroups() = IngredientDiagnosticGroups(
    veganIngredientIds = matched.filter { it.status == VeganStatus.VEGAN }.map { it.id },
    vegetarianIngredientIds = matched.filter { it.status == VeganStatus.VEGETARIAN }.map { it.id },
    nonVegetarianIngredientIds = matched.filter { it.status == VeganStatus.NON_VEGAN }.map { it.id },
    uncertainIngredientIds = matched.filter { it.status == VeganStatus.UNCERTAIN }.map { it.id },
    unknownIngredients = unknown
)

internal fun AnalysisResult.toDecisionDiagnostic(): DecisionDiagnostic {
    val reason = when {
        availability == AnalysisAvailability.NO_INGREDIENT_LIST && matched.isEmpty() ->
            DecisionReason.NO_INGREDIENT_LIST
        originNonVeganIngredientIds.isNotEmpty() -> DecisionReason.EXPLICIT_NON_VEGAN_ORIGIN
        veganBlockers.isNotEmpty() -> DecisionReason.KNOWN_VEGAN_BLOCKER
        uncertainIngredients.isNotEmpty() -> DecisionReason.UNCERTAIN_INGREDIENT
        unknown.isNotEmpty() -> DecisionReason.UNKNOWN_INGREDIENT
        matched.isEmpty() -> DecisionReason.NO_RECOGNIZED_INGREDIENT
        else -> DecisionReason.ALL_RECOGNIZED_INGREDIENTS_VEGAN
    }
    val responsibleIds = when (reason) {
        DecisionReason.EXPLICIT_NON_VEGAN_ORIGIN ->
            (originNonVeganIngredientIds + veganBlockers.map { it.id }).distinct()
        DecisionReason.KNOWN_VEGAN_BLOCKER -> veganBlockers.map { it.id }
        DecisionReason.UNCERTAIN_INGREDIENT -> uncertainIngredients.map { it.id }
        DecisionReason.ALL_RECOGNIZED_INGREDIENTS_VEGAN -> matched.map { it.id }
        else -> emptyList()
    }
    return DecisionDiagnostic(
        veganAssessment = veganAssessment,
        detailedVerdict = verdict,
        vegetarianVerdictWithoutUncertain = vegetarianVerdictWithoutUncertain,
        reason = reason,
        responsibleIngredientIds = responsibleIds,
        unknownIngredients = unknown,
        unknownPreventsVegan = unknown.isNotEmpty() && veganAssessment == VeganAssessment.UNCERTAIN,
        tracesExcludedFromVerdict = true
    )
}

internal fun AnalysisDiagnostics.toVerdictExplanation(): VerdictExplanation {
    val tokensByOrder = tokens.associateBy { it.order }
    fun pathFor(token: TokenDiagnostic): List<String> {
        val reversed = mutableListOf<String>()
        var current: TokenDiagnostic? = token
        val visited = hashSetOf<Int>()
        while (current != null && visited.add(current.order)) {
            reversed += current.text
            current = current.parentOrder?.let(tokensByOrder::get)
        }
        return reversed.asReversed()
    }

    fun referencesFor(ingredients: List<Ingredient>): List<VerdictIngredientReference> {
        val byId = ingredients.associateBy { it.id }
        val occurrences = tokens.flatMap { token ->
            token.matchedIngredientIds.mapIndexedNotNull { index, id ->
                byId[id]?.takeIf { ingredient ->
                    token.effectiveStatuses.getOrNull(index)?.let { it == ingredient.status } ?: true
                }?.let { ingredient ->
                    VerdictIngredientReference(
                        occurrenceId = "token:${token.order}:match:$index",
                        ingredientId = ingredient.id,
                        displayName = ingredient.eNumber ?: ingredient.name,
                        path = pathFor(token),
                        status = ingredient.status,
                        reason = ingredient.reason
                    )
                }
            }
        }
        val representedIds = occurrences.mapTo(hashSetOf()) { it.ingredientId }
        return occurrences + ingredients.filterNot { it.id in representedIds }.map { ingredient ->
            VerdictIngredientReference(
                occurrenceId = "ingredient:${ingredient.id}",
                ingredientId = ingredient.id,
                displayName = ingredient.eNumber ?: ingredient.name,
                path = listOf(ingredient.eNumber ?: ingredient.name),
                status = ingredient.status,
                reason = ingredient.reason
            )
        }
    }

    val uncertain = referencesFor(result.uncertainIngredients)
    val explicitOriginBlockers = result.matched.filter { it.id in result.originNonVeganIngredientIds }
    val knownBlockers = referencesFor((result.veganBlockers + explicitOriginBlockers).distinctBy { it.id })
    val conditionalCandidate = result.verdictWithoutUncertain
    val conditionalReason = when {
        result.availability == AnalysisAvailability.NO_INGREDIENT_LIST || result.verdict == null ->
            ConditionalVerdictReason.NO_RELIABLE_ANALYSIS
        uncertain.isEmpty() -> ConditionalVerdictReason.NO_UNCERTAIN_INGREDIENT
        result.originNonVeganIngredientIds.isNotEmpty() ||
            conditionalCandidate == AnalysisVerdict.NON_VEGETARIAN ->
            ConditionalVerdictReason.KNOWN_NON_VEGETARIAN_INGREDIENT_REMAINS
        conditionalCandidate == AnalysisVerdict.INCONCLUSIVE ->
            if (result.unknown.isNotEmpty()) ConditionalVerdictReason.UNKNOWN_INGREDIENT_REMAINS
            else ConditionalVerdictReason.NO_RELIABLE_ANALYSIS
        else -> ConditionalVerdictReason.UNCERTAIN_INGREDIENTS_EXCLUDED
    }
    val conditionalVerdict = conditionalCandidate.takeIf {
        conditionalReason == ConditionalVerdictReason.UNCERTAIN_INGREDIENTS_EXCLUDED &&
            it in setOf(AnalysisVerdict.VEGAN, AnalysisVerdict.VEGETARIAN)
    }
    return VerdictExplanation(
        mainVerdict = result.verdict,
        mainVeganAssessment = result.veganAssessment,
        knownBlockingIngredients = knownBlockers,
        uncertainIngredients = uncertain,
        conditionalVerdict = conditionalVerdict,
        conditionalReason = conditionalReason,
        vegetarianStatus = result.vegetarianVerdictWithoutUncertain,
        tracesExcludedFromConditionalVerdict = true
    )
}

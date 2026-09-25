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

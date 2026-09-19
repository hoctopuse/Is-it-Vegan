package com.example.isitvegan

internal object VerdictEngine {
    fun assessVeganCompatibility(
        matched: List<Ingredient>,
        unknown: List<String>
    ): VeganAssessment = when {
        matched.any {
            it.status == VeganStatus.VEGETARIAN || it.status == VeganStatus.NON_VEGAN
        } -> VeganAssessment.NOT_VEGAN
        matched.any { it.status == VeganStatus.UNCERTAIN } || unknown.isNotEmpty() || matched.isEmpty() ->
            VeganAssessment.UNCERTAIN
        else -> VeganAssessment.VEGAN
    }

    fun evaluate(
        matched: List<Ingredient>,
        unknown: List<String>,
        excludeUncertain: Boolean = false
    ): AnalysisVerdict {
        val considered = if (excludeUncertain) {
            matched.filter { it.status != VeganStatus.UNCERTAIN }
        } else {
            matched
        }
        return when {
            considered.any { it.status == VeganStatus.NON_VEGAN } ->
                AnalysisVerdict.NON_VEGETARIAN
            !excludeUncertain && considered.any { it.status == VeganStatus.UNCERTAIN } ->
                AnalysisVerdict.UNCERTAIN
            unknown.isNotEmpty() || considered.isEmpty() ->
                AnalysisVerdict.INCONCLUSIVE
            considered.any { it.status == VeganStatus.VEGETARIAN } ->
                AnalysisVerdict.VEGETARIAN
            else -> AnalysisVerdict.VEGAN
        }
    }

    fun isDecisiveNonVegetarian(ingredients: List<Ingredient>): Boolean =
        ingredients.any { it.status == VeganStatus.NON_VEGAN }
}

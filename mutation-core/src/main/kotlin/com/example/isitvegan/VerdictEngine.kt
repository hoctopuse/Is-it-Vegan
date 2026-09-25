package com.example.isitvegan

object VerdictEngine {
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

    /** Does not alter vegan analysis: it only reports the known non-uncertain ingredients. */
    fun evaluateVegetarianCompatibility(matched: List<Ingredient>): AnalysisVerdict {
        val considered = matched.filter { it.status != VeganStatus.UNCERTAIN }
        return when {
            considered.any { it.status == VeganStatus.NON_VEGAN } -> AnalysisVerdict.NON_VEGETARIAN
            considered.isEmpty() -> AnalysisVerdict.INCONCLUSIVE
            else -> AnalysisVerdict.VEGETARIAN
        }
    }
}

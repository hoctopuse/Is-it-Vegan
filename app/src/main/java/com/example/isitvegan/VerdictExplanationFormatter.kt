package com.example.isitvegan

import android.content.res.Resources

/** Shared localized rendering of the structured verdict explanation. */
internal object VerdictExplanationFormatter {
    fun render(
        resources: Resources,
        input: String,
        diagnostics: AnalysisDiagnostics,
        includeAnalysisContext: Boolean = false
    ): String {
        val detailed = renderDetailed(resources, input, diagnostics)
        val result = if (
            includeAnalysisContext &&
            diagnostics.result.availability != AnalysisAvailability.NO_INGREDIENT_LIST
        ) {
            resources.getString(
                R.string.vegan_compatibility_format,
                veganAssessment(resources, diagnostics.result.veganAssessment)
            ) + "\n\n" + resources.getString(R.string.detailed_classification_title) +
                "\n" + detailed + "\n\n" + resources.getString(R.string.analysis_declared_notice)
        } else {
            detailed
        }
        return result + renderTraces(resources, diagnostics.result.crossContactWarnings)
    }

    fun renderUncertain(resources: Resources, diagnostics: AnalysisDiagnostics): String =
        renderDetailed(resources, diagnostics.input, diagnostics)

    private fun renderDetailed(
        resources: Resources,
        input: String,
        diagnostics: AnalysisDiagnostics
    ): String {
        val result = diagnostics.result
        if (input.isBlank()) return resources.getString(R.string.empty_input_prompt)
        if (result.availability == AnalysisAvailability.NO_INGREDIENT_LIST) {
            val sections = mutableListOf(
                resources.getString(R.string.no_ingredient_list),
                resources.getString(R.string.no_vegan_conclusion),
                resources.getString(R.string.analysis_not_evaluated)
            )
            if (result.declaredPresenceIngredientIds.isNotEmpty()) {
                sections += resources.getString(
                    R.string.declared_presence_format,
                    result.declaredPresenceIngredientIds.joinToString(" | ") { it.replace("|", "\\|") }
                )
            }
            return sections.joinToString("\n\n")
        }

        val explanation = diagnostics.verdictExplanation
        val sections = mutableListOf<String>()
        sections += when (result.verdict) {
            AnalysisVerdict.NON_VEGETARIAN -> resources.getString(R.string.verdict_non_vegan)
            AnalysisVerdict.UNCERTAIN -> resources.getString(R.string.verdict_uncertain)
            AnalysisVerdict.INCONCLUSIVE -> resources.getString(R.string.verdict_inconclusive)
            AnalysisVerdict.VEGETARIAN -> resources.getString(R.string.verdict_vegetarian)
            AnalysisVerdict.VEGAN, null -> resources.getString(R.string.verdict_vegan)
        }

        if (explanation.knownBlockingIngredients.isNotEmpty()) {
            sections += resources.getString(R.string.known_blocking_ingredients_title) + "\n" +
                explanation.knownBlockingIngredients.joinToString(" | ") {
                    resources.getString(
                        R.string.detected_ingredient_item,
                        it.path.joinToString(" → ").replace("|", "\\|")
                    )
                }
        }
        if (explanation.uncertainIngredients.isNotEmpty()) {
            sections += resources.getString(R.string.uncertain_ingredients_title) + "\n" +
                explanation.uncertainIngredients.joinToString(" | ") {
                    resources.getString(
                        R.string.uncertain_ingredient_item,
                        it.path.joinToString(" → ").replace("|", "\\|")
                    )
                }
        }
        if (result.unknown.isNotEmpty()) {
            sections += resources.getString(R.string.unidentified_ingredients_title) + "\n" +
                result.unknown.joinToString(" | ") {
                    resources.getString(R.string.unidentified_ingredient_item, it.replace("|", "\\|"))
                }
        }

        when (explanation.conditionalVerdict) {
            AnalysisVerdict.VEGAN -> sections += resources.getString(R.string.conditional_vegan)
            AnalysisVerdict.VEGETARIAN -> sections += resources.getString(R.string.conditional_vegetarian)
            else -> Unit
        }
        when (result.verdict) {
            AnalysisVerdict.INCONCLUSIVE -> sections += resources.getString(R.string.inconclusive_explanation)
            AnalysisVerdict.VEGETARIAN -> sections += resources.getString(R.string.vegetarian_explanation)
            AnalysisVerdict.VEGAN -> sections += resources.getString(R.string.vegan_explanation)
            else -> Unit
        }
        return sections.joinToString("\n\n")
    }

    private fun renderTraces(resources: Resources, warnings: List<String>): String {
        if (warnings.isEmpty()) return ""
        val items = warnings.joinToString(" | ") {
            resources.getString(R.string.trace_item, it.replace("|", "\\|"))
        }
        return "\n\n" + resources.getString(R.string.traces_title) + "\n" + items +
            "\n\n" + resources.getString(R.string.traces_excluded_notice)
    }

    private fun veganAssessment(resources: Resources, assessment: VeganAssessment): String =
        when (assessment) {
            VeganAssessment.VEGAN -> resources.getString(R.string.assessment_vegan)
            VeganAssessment.NOT_VEGAN -> resources.getString(R.string.assessment_non_vegan)
            VeganAssessment.UNCERTAIN -> resources.getString(R.string.assessment_uncertain)
        }
}

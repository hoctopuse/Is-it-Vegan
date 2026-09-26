package com.example.isitvegan

import android.content.res.Resources
import java.text.NumberFormat

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
                    result.declaredPresenceIngredientIds.joinToString("\n") { it.displayText() }
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
                explanation.knownBlockingIngredients.joinToString("\n") {
                    resources.getString(
                        R.string.detected_ingredient_item,
                        it.path.joinToString(" → ").displayText()
                    )
                }
        }
        if (explanation.uncertainIngredients.isNotEmpty()) {
            sections += resources.getString(R.string.uncertain_ingredients_title) + "\n" +
                explanation.uncertainIngredients.joinToString("\n") {
                    resources.getString(
                        R.string.uncertain_ingredient_item,
                        it.path.joinToString(" → ").displayText()
                    )
                }
        }
        if (result.verdict == AnalysisVerdict.INCONCLUSIVE && result.unknown.isNotEmpty()) {
            sections += renderUnknownIngredients(resources, diagnostics)
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
        val items = warnings.joinToString("\n") {
            resources.getString(R.string.trace_item, it.replace("|", "¦"))
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

    private fun renderUnknownIngredients(resources: Resources, diagnostics: AnalysisDiagnostics): String {
        val tokensByOrder = diagnostics.tokens.associateBy { it.order }
        val occurrences = diagnostics.tokens.filter { it.unknown != null && !it.isDeclaredPresence }
        val entries = mutableListOf<UnknownEntry>()
        occurrences.forEach { token ->
            val parent = token.parentOrder?.let(tokensByOrder::get)
            if (parent == null) {
                entries += UnknownEntry.Simple(token.unknown.orEmpty())
            } else {
                val group = entries.filterIsInstance<UnknownEntry.Nested>()
                    .firstOrNull { it.parent.order == parent.order }
                if (group == null) {
                    entries += UnknownEntry.Nested(parent, mutableListOf(token))
                } else {
                    group.children += token
                }
            }
        }
        if (entries.isEmpty()) {
            diagnostics.result.unknown.forEach { entries += UnknownEntry.Simple(it) }
        }
        val items = entries.joinToString("\n") { entry ->
            when (entry) {
                is UnknownEntry.Simple -> resources.getString(
                    R.string.unidentified_ingredient_item,
                    entry.text.displayText()
                )
                is UnknownEntry.Nested -> {
                    val parent = resources.getString(
                        R.string.unidentified_parent_item,
                        entry.parent.text.displayTextWithPercentage(resources, entry.parent.quantityPercent)
                    )
                    parent + entry.children.joinToString("") { child ->
                        "\n" + resources.getString(
                            R.string.unidentified_child_item,
                            child.unknown.orEmpty().displayTextWithPercentage(resources, child.quantityPercent)
                        )
                    }
                }
            }
        }
        return resources.getString(R.string.unidentified_ingredients_title) + "\n" + items +
            "\n\n" + resources.getString(R.string.unidentified_ingredients_notice)
    }

    private fun String.displayText(): String = replace("|", "¦")

    private fun String.displayTextWithPercentage(resources: Resources, percentage: java.math.BigDecimal?): String {
        if (percentage == null) return displayText()
        val formatted = NumberFormat.getNumberInstance(resources.configuration.locales[0])
            .format(percentage)
        return resources.getString(R.string.ingredient_with_percentage, displayText(), formatted)
    }

    private sealed interface UnknownEntry {
        data class Simple(val text: String) : UnknownEntry
        data class Nested(
            val parent: TokenDiagnostic,
            val children: MutableList<TokenDiagnostic>
        ) : UnknownEntry
    }
}

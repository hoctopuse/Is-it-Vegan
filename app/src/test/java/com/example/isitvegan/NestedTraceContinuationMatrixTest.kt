package com.example.isitvegan

import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Every row runs extraction, preprocessing, matching and both verdicts with shipped assets. */
@RunWith(Parameterized::class)
class NestedTraceContinuationMatrixTest(private val case: Case, private val mode: InputMode) {
    data class Case(
        val name: String,
        val text: String,
        val term: String,
        val ambiguous: Boolean,
        val presence: Boolean,
        val wellDelimited: Boolean = true
    ) {
        override fun toString() = "$name/${if (ambiguous) "AMBIGUOUS" else "UNDERSTOOD"}"
    }

    @Test fun scopeAndVerdict() {
        val d = service.analyzeWithDiagnostics(case.text, mode)
        val extracted = if (case.presence) d.labelSections.declaredContainsText else d.labelSections.ingredientsText
        assertTrue("suffix retained: $extracted", extracted.orEmpty().contains(case.term))
        assertEquals(case.text, d.labelSections.rawText)
        assertTrue(d.crossContactWarnings.any { it.contains("noix") })
        if (case.wellDelimited) assertFalse("trace leaked into composition", extracted.orEmpty().contains("Peut contenir"))
        if (case.wellDelimited) assertTrue("isolated delimiter", IngredientTreeParser.inspectParentheses(extracted.orEmpty()).balanced)
        if (case.ambiguous) {
            assertTrue("scope must be ambiguous", d.ambiguousOcrText)
            assertNotEquals(VeganAssessment.VEGAN, d.result.veganAssessment)
            assertNotNull(d.availabilityReason)
        } else {
            assertFalse("scope must be understood", d.ambiguousOcrText)
            val (id, detailed, vegan) = when (case.term) {
                "lait" -> Triple("milk", AnalysisVerdict.VEGETARIAN, VeganAssessment.NOT_VEGAN)
                "E471" -> Triple("e471", AnalysisVerdict.UNCERTAIN, VeganAssessment.UNCERTAIN)
                "mystere" -> Triple(null, AnalysisVerdict.INCONCLUSIVE, VeganAssessment.UNCERTAIN)
                else -> Triple("sugar", AnalysisVerdict.VEGAN, VeganAssessment.VEGAN)
            }
            if (id != null) assertTrue("recognized $id", d.result.matched.any { it.id == id })
            else assertTrue(d.visibleUnknownIngredients.any { it.contains(case.term) })
            val expectedIds = setOfNotNull("water", "soy".takeIf { case.presence }, id)
            assertEquals("ingredients before and after trace", expectedIds, d.result.matched.map { it.id }.toSet())
            assertEquals(detailed, d.result.verdict)
            assertEquals(vegan, d.result.veganAssessment)
            assertFalse("trace term in unknowns", d.result.unknown.any { it.contains("noix") || it.contains("amandes") })
            assertFalse("suffix absorbed in trace", d.crossContactWarnings.any { it.contains(case.term) })
        }
        d.labelSections.ingredientSection?.let { section ->
            assertEquals(section.rawText, d.labelSections.rawText.substring(section.start, section.end))
        }
    }

    companion object {
        private val service by lazy {
            IngredientAnalysisService(IngredientKnowledge.fromJson(
                File("src/main/assets/ingredients.json").readText(),
                File("src/main/assets/ingredient_aliases_multilingual.json").readText(),
                File("src/main/assets/origin_qualifier_rules.json").readText()
            ))
        }

        @JvmStatic @Parameterized.Parameters(name = "{index}: {0} {1}")
        fun cases(): List<Array<Any>> = buildList {
            val delimiters = listOf("(" to ")", "[" to "]", "([" to "])", "[(" to ")]")
            val separators = listOf(
                "comma" to ", ", "semicolon" to "; ", "period" to ". ",
                "dash" to " - ", "en-dash" to " \u2013 ", "em-dash" to " \u2014 ",
                "newline" to "\n", "spaces" to "  ", "none" to "", "unexpected" to " / "
            )
            for ((open, close) in delimiters) for (term in listOf("lait", "E471", "mystere", "sucre")) {
                for (context in listOf("composition", "presence", "manual")) for (position in listOf("start", "middle", "end")) {
                    val presence = context == "presence"
                    val prefix = when (context) {
                        "presence" -> "Ingredients: eau. Contient:"
                        "manual" -> ""
                        else -> "Ingredients:"
                    }
                    val before = if (presence) "soja" else "eau"
                    val group = "${open}Peut contenir: noix (amandes)$close"
                    for ((separatorName, separator) in if (position == "end") listOf("end" to "") else separators) {
                        val text = when (position) {
                            "start" -> "$prefix $group$separator$term, $before."
                            "middle" -> "$prefix $before $group$separator$term."
                            else -> "$prefix $before, $term $group."
                        }.trimStart()
                        val ambiguous = separatorName in listOf("spaces", "none", "unexpected")
                        val case = Case("$open$close/$position/$separatorName/$term/$context", text, term, ambiguous, presence)
                        val modes = if (context == "manual") listOf(InputMode.MANUAL_INGREDIENT_LIST) else InputMode.values().toList()
                        for (mode in modes) add(arrayOf(case, mode))
                    }
                }
            }
            val broken = listOf("(" to "]", "[" to ")", "([" to ")]", "(" to "", "[" to "", "(" to "))")
            for ((open, close) in broken) for (term in listOf("lait", "E471", "mystere", "sucre")) {
                for (presence in listOf(false, true)) {
                    val prefix = if (presence) "Ingredients: eau. Contient: soja" else "Ingredients: eau"
                    val case = Case("broken/$open$close/$term/presence=$presence",
                        "$prefix ${open}Peut contenir: noix$close, $term.", term, true, presence, false)
                    for (mode in InputMode.values()) add(arrayOf(case, mode))
                }
            }
        }
    }
}

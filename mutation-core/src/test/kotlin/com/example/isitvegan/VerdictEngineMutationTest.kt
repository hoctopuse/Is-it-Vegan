package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class VerdictEngineMutationTest {
    private val vegan = ingredient("vegan", VeganStatus.VEGAN)
    private val vegetarian = ingredient("vegetarian", VeganStatus.VEGETARIAN)
    private val nonVegan = ingredient("non_vegan", VeganStatus.NON_VEGAN)
    private val uncertain = ingredient("uncertain", VeganStatus.UNCERTAIN)

    @Test fun veganAssessmentClassifiesEveryStatusWithoutAnotherCauseMaskingIt() {
        val cases = listOf(
            emptyList<Ingredient>() to VeganAssessment.UNCERTAIN,
            listOf(vegan) to VeganAssessment.VEGAN,
            listOf(uncertain) to VeganAssessment.UNCERTAIN,
            listOf(vegetarian) to VeganAssessment.NOT_VEGAN,
            listOf(nonVegan) to VeganAssessment.NOT_VEGAN
        )

        cases.forEach { (matched, expected) ->
            assertEquals(matched.map { it.status }.toString(), expected,
                VerdictEngine.assessVeganCompatibility(matched, emptyList()))
        }
        assertEquals(
            VeganAssessment.UNCERTAIN,
            VerdictEngine.assessVeganCompatibility(listOf(vegan), listOf("unknown"))
        )
    }

    @Test fun veganAssessmentFindsDecisiveStatusesAtEitherEndOfTheList() {
        listOf(
            listOf(vegetarian, vegan, uncertain),
            listOf(vegan, uncertain, vegetarian),
            listOf(nonVegan, vegan, uncertain),
            listOf(vegan, uncertain, nonVegan)
        ).forEach { matched ->
            assertEquals(matched.map { it.status }.toString(), VeganAssessment.NOT_VEGAN,
                VerdictEngine.assessVeganCompatibility(matched, emptyList()))
        }

        listOf(
            listOf(uncertain, vegan, vegan),
            listOf(vegan, vegan, uncertain)
        ).forEach { matched ->
            assertEquals(matched.map { it.status }.toString(), VeganAssessment.UNCERTAIN,
                VerdictEngine.assessVeganCompatibility(matched, emptyList()))
        }
    }

    @Test fun detailedVerdictKeepsPrecedenceAndExcludeUncertainSemantics() {
        val cases = listOf(
            Triple(emptyList<Ingredient>(), emptyList<String>(), AnalysisVerdict.INCONCLUSIVE),
            Triple(listOf(vegan), emptyList(), AnalysisVerdict.VEGAN),
            Triple(listOf(vegetarian), emptyList(), AnalysisVerdict.VEGETARIAN),
            Triple(listOf(uncertain), emptyList(), AnalysisVerdict.UNCERTAIN),
            Triple(listOf(nonVegan), emptyList(), AnalysisVerdict.NON_VEGETARIAN),
            Triple(listOf(vegan), listOf("unknown"), AnalysisVerdict.INCONCLUSIVE),
            Triple(listOf(vegan, uncertain), emptyList(), AnalysisVerdict.UNCERTAIN),
            Triple(listOf(vegan, vegetarian), emptyList(), AnalysisVerdict.VEGETARIAN),
            Triple(listOf(vegan, uncertain, nonVegan), listOf("unknown"), AnalysisVerdict.NON_VEGETARIAN)
        )

        cases.forEach { (matched, unknown, expected) ->
            assertEquals(matched.map { it.status }.toString(), expected,
                VerdictEngine.evaluate(matched, unknown))
        }

        assertEquals(AnalysisVerdict.INCONCLUSIVE,
            VerdictEngine.evaluate(listOf(uncertain), emptyList(), excludeUncertain = true))
        assertEquals(AnalysisVerdict.VEGAN,
            VerdictEngine.evaluate(listOf(uncertain, vegan), emptyList(), excludeUncertain = true))
        assertEquals(AnalysisVerdict.NON_VEGETARIAN,
            VerdictEngine.evaluate(listOf(uncertain, nonVegan), emptyList(), excludeUncertain = true))
    }

    @Test fun vegetarianCompatibilityFiltersOnlyUncertainAndIsOrderIndependent() {
        val cases = listOf(
            emptyList<Ingredient>() to AnalysisVerdict.INCONCLUSIVE,
            listOf(uncertain) to AnalysisVerdict.INCONCLUSIVE,
            listOf(vegan) to AnalysisVerdict.VEGETARIAN,
            listOf(vegetarian) to AnalysisVerdict.VEGETARIAN,
            listOf(nonVegan) to AnalysisVerdict.NON_VEGETARIAN,
            listOf(uncertain, vegan) to AnalysisVerdict.VEGETARIAN,
            listOf(vegan, uncertain) to AnalysisVerdict.VEGETARIAN,
            listOf(uncertain, nonVegan, vegan) to AnalysisVerdict.NON_VEGETARIAN,
            listOf(vegan, nonVegan, uncertain) to AnalysisVerdict.NON_VEGETARIAN
        )

        cases.forEach { (matched, expected) ->
            assertEquals(matched.map { it.status }.toString(), expected,
                VerdictEngine.evaluateVegetarianCompatibility(matched))
        }
    }

    private fun ingredient(id: String, status: VeganStatus) =
        Ingredient(id, id, listOf(id), null, status, "Test")
}

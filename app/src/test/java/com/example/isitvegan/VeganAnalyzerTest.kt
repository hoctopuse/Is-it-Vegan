package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class VeganAnalyzerTest {
    private val database = listOf(
        ingredient("sucre", "sucre", VeganStatus.VEGAN),
        ingredient("farine", "farine de blé", VeganStatus.VEGAN),
        ingredient("lait", "lait", VeganStatus.VEGETARIAN),
        ingredient("coco", "lait de coco", VeganStatus.VEGAN),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN, "E471"),
        ingredient("e627", "guanylate de sodium", VeganStatus.UNCERTAIN, "E627")
    )

    @Test fun allIngredientsMustBeCovered() {
        val result = VeganAnalyzer.analyze("Ingrédients: sucre, farine de blé", database)
        assertEquals(2, result.matched.size)
        assertEquals(emptyList<String>(), result.unknown)
        assertEquals(listOf("mystère"), VeganAnalyzer.analyze("sucre, mystère", database).unknown)
    }

    @Test fun nestedIngredientsAndOrigins() {
        val result = VeganAnalyzer.analyze("sucre (lait de coco; E 471)", database)
        assertEquals(listOf("sucre", "coco", "e471"), result.matched.map { it.id })
        assertEquals(emptyList<String>(), result.unknown)
        assertEquals(VeganStatus.VEGETARIAN,
            VeganAnalyzer.analyze("lait", database).matched.single().status)
    }

    @Test fun eNumbersMayContainASpace() {
        val result = VeganAnalyzer.analyze("sucre, E 627", database)
        assertEquals(listOf("sucre", "e627"), result.matched.map { it.id })
        assertEquals(VeganStatus.UNCERTAIN, result.matched.last().status)
        assertEquals(emptyList<String>(), result.unknown)
    }

    @Test fun verdictsRespectUnknownAndAmbiguousIngredients() {
        val samples = listOf(
            "sucre, farine de blé" to AnalysisVerdict.VEGAN,
            "sucre, lait" to AnalysisVerdict.VEGETARIAN,
            "sucre, lait, mystère" to AnalysisVerdict.INCONCLUSIVE,
            "sucre, lait, E471" to AnalysisVerdict.UNCERTAIN,
            "sucre, gélatine, lait" to AnalysisVerdict.NON_VEGETARIAN
        )
        val entries = database + ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        samples.forEach { (text, expected) ->
            assertEquals(text, expected, VeganAnalyzer.analyze(text, entries).verdict)
        }
    }

    @Test fun uncertainVerdictExplainsTheRemainder() {
        val veganRemainder = VeganAnalyzer.analyze("sucre, E471, farine de blé", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, veganRemainder.verdict)
        assertEquals(AnalysisVerdict.VEGAN, veganRemainder.verdictWithoutUncertain)
        assertEquals(listOf("e471"), veganRemainder.uncertainIngredients.map { it.id })
        assertEquals(emptyList<String>(), veganRemainder.vegetarianIngredients.map { it.id })

        val vegetarianRemainder = VeganAnalyzer.analyze("farine de blé, lait, E471, sucre", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, vegetarianRemainder.verdict)
        assertEquals(AnalysisVerdict.VEGETARIAN, vegetarianRemainder.verdictWithoutUncertain)
        assertEquals(listOf("e471"), vegetarianRemainder.uncertainIngredients.map { it.id })
        assertEquals(listOf("lait"), vegetarianRemainder.vegetarianIngredients.map { it.id })
    }

    @Test fun unknownIngredientsPreventValidationOfTheRemainder() {
        val result = VeganAnalyzer.analyze("sucre, E471, mystère", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdictWithoutUncertain)
    }

    @Test fun tracesAndAllergenNotesAreNotIngredients() {
        val sample = """Eau, graines de soja*, présure (nigari), chlorure de calcium. *Agriculture biologique.
            **Allergènes :** soja
            **Traces :** lait, gélatine""".trimIndent()
        val entries = database + ingredient("eau", "eau", VeganStatus.VEGAN) +
            ingredient("soja", "graines de soja", VeganStatus.VEGAN) +
            ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        val result = VeganAnalyzer.analyze(sample, entries)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
        assertEquals(listOf("eau", "soja"), result.matched.map { it.id })
        assertEquals(listOf("présure", "nigari", "chlorure de calcium."), result.unknown)
    }

    @Test fun inlineCrossContactNotesDoNotAffectTheVerdict() {
        val entries = database + ingredient("gelatine", "gélatine", VeganStatus.NON_VEGAN)
        val result = VeganAnalyzer.analyze(
            "sucre, E471, farine de blé. Peut contenir des traces de gélatine et de lait",
            entries
        )
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.VEGAN, result.verdictWithoutUncertain)
        assertEquals(listOf("e471"), result.uncertainIngredients.map { it.id })
        assertEquals(false, result.stoppedAtNonVegetarian)
    }

    @Test fun animalIngredientDominatesUnknownsAndAmbiguity() {
        val entries = database + ingredient("porc", "viande de porc", VeganStatus.NON_VEGAN) +
            ingredient("jaune", "jaune d’œuf", VeganStatus.VEGETARIAN)
        val result = VeganAnalyzer.analyze(
            "viande de porc (64%) (origine: Belgique); jaune d’OEUF; E471; ingrédient inconnu",
            entries
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertEquals(listOf("porc"), result.matched.map { it.id })
        assertEquals(true, result.stoppedAtNonVegetarian)
        assertEquals(emptyList<String>(), result.unknown)
    }

    private fun ingredient(id: String, alias: String, status: VeganStatus, number: String? = null) =
        Ingredient(id, alias, listOf(alias), number, status, "Test")
}

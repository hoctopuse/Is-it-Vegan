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

    private fun ingredient(id: String, alias: String, status: VeganStatus, number: String? = null) =
        Ingredient(id, alias, listOf(alias), number, status, "Test")
}

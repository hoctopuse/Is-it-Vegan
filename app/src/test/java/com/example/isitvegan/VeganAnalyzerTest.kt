package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class VeganAnalyzerTest {
    private val database = listOf(
        ingredient("sucre", "sucre", VeganStatus.VEGAN),
        ingredient("farine", "farine de blé", VeganStatus.VEGAN),
        ingredient("lait", "lait", VeganStatus.NON_VEGAN),
        ingredient("coco", "lait de coco", VeganStatus.VEGAN),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN, "E471")
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
        assertEquals(VeganStatus.NON_VEGAN,
            VeganAnalyzer.analyze("lait", database).matched.single().status)
    }

    private fun ingredient(id: String, alias: String, status: VeganStatus, number: String? = null) =
        Ingredient(id, alias, listOf(alias), number, status, "Test")
}

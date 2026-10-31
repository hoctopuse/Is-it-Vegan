package com.example.isitvegan

import org.junit.Assert.*
import org.junit.Test

class UnresolvedExplanationTest {
    @Test fun partialKnownIngredientStillExplainsWhyVerdictIsInconclusive() {
        val service = IngredientAnalysisService(IngredientKnowledge(listOf(
            Ingredient("tomato", "tomate", listOf("tomate"), status = VeganStatus.VEGAN, reason = "test")
        )))
        val result = service.analyzeWithDiagnostics("tomate inhabituelle. Peut contenir : lait.", InputMode.MANUAL_INGREDIENT_LIST)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.result.verdict)
        assertTrue(result.result.unknown.any { it.contains("inhabituelle") })
        assertTrue(result.visibleUnknownIngredients.any { it.contains("inhabituelle") })
        assertTrue(result.result.crossContactWarnings.any { it.contains("lait") })
        assertFalse(result.visibleUnknownIngredients.any { it.contains("lait") })
    }

    @Test fun unknownDeclaredPresenceIsVisibleWhileTracesRemainSeparate() {
        val service = IngredientAnalysisService(IngredientKnowledge(listOf(
            Ingredient("water", "eau", listOf("eau"), status = VeganStatus.VEGAN, reason = "test")
        )))
        val result = service.analyzeWithDiagnostics("Ingrédients: eau. Contient: mystère. Peut contenir: lait.", InputMode.FULL_LABEL)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.result.verdict)
        assertTrue(result.result.unknown.any { it.contains("mystère") })
        assertTrue(result.visibleUnknownIngredients.any { it.contains("mystère") })
        assertFalse(result.visibleUnknownIngredients.any { it.contains("lait") })
        assertTrue(result.crossContactWarnings.any { it.contains("lait") })
    }

    @Test fun declaredUnknownWithoutListDoesNotProduceAVerdict() {
        val result = IngredientAnalysisService(IngredientKnowledge.empty()).analyzeWithDiagnostics(
            "Contient: mystère. Peut contenir: lait.", InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, result.result.availability)
        assertNull(result.result.verdict)
        assertTrue(result.visibleUnknownIngredients.any { it.contains("mystère") })
        assertFalse(result.visibleUnknownIngredients.any { it.contains("lait") })
        assertTrue(result.crossContactWarnings.any { it.contains("lait") })
    }
}

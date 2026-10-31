package com.example.isitvegan

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class UnresolvedRealKnowledgeTest {
    @Test fun partialMatchInRealKnowledgeRemainsVisibleAndDoesNotIncludeTraces() {
        val knowledge = IngredientKnowledge.fromJson(
            File("src/main/assets/ingredients.json").readText(),
            File("src/main/assets/ingredient_aliases_multilingual.json").readText(),
            File("src/main/assets/origin_qualifier_rules.json").readText()
        )
        val result = IngredientAnalysisService(knowledge).analyzeWithDiagnostics(
            "Ingrédients: eau inhabituelle. Peut contenir : lait.", InputMode.FULL_LABEL
        )
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.result.verdict)
        assertTrue(result.result.unknown.any { it.contains("inhabituelle") })
        assertTrue(result.visibleUnknownIngredients.any { it.contains("inhabituelle") })
        assertTrue(result.crossContactWarnings.any { it.contains("lait") })
        assertFalse(result.visibleUnknownIngredients.any { it.contains("lait") })
    }
}

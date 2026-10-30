package com.example.isitvegan

import org.junit.Assert.*
import org.junit.Test

class OcrBoundarySafetyTest {
    private val service = IngredientAnalysisService(IngredientKnowledge(listOf(
        ingredient("water", "eau", VeganStatus.VEGAN),
        ingredient("sugar", "sucre", VeganStatus.VEGAN),
        ingredient("milk", "MILCH", VeganStatus.NON_VEGAN)
    )))
    private fun ingredient(id: String, name: String, status: VeganStatus) =
        Ingredient(id, name, listOf(name), status = status, reason = "test")
    private val original = "Ingrédients: eau, sucre"
    private val id = stableBlockId(original)
    private fun analyze(text: String, blockId: String? = id) =
        service.analyzeWithDiagnostics(text, InputMode.OCR_LABEL, UiLanguage.FR, blockId)

    private fun assertUnsafe(text: String, blockId: String? = id) {
        val result = analyze(text, blockId)
        assertEquals(text, result.input)
        assertEquals(text, result.languageSegmentation.selectedText)
        assertEquals(text, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.ambiguousOcrText)
        assertNull(result.result.verdict)
        assertTrue(result.preprocessedInput.isEmpty())
    }

    @Test fun staleSourceIdCannotAuthorizeReplacementWithSeveralLists() {
        assertUnsafe("Ingrédients: eau, sucre\nZUTATEN: MILCH, mystère")
    }

    @Test fun staleIdDoesNotBlockSingleEditedList() {
        val text = "$original, MILCH, mystère"
        val result = analyze(text)
        assertEquals(text, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.result.verdict)
        assertTrue(result.result.unknown.any { it.contains("mystère") })
    }

    @Test fun languageMarkersInsideParenthesesOrBracketsAreUnsafe() {
        listOf(
            "Ingrédients: eau (DE: ZUTATEN: sucre), MILCH, mystère. Peut contenir: sucre.",
            "Ingrédients: eau [DE: ZUTATEN: sucre], MILCH, mystère. Peut contenir: sucre.",
            "Ingrédients: eau (DE: sucre), MILCH, mystère. Peut contenir: sucre.",
            "Ingrédients: eau [DE: sucre], MILCH, mystère. Peut contenir: sucre.",
            "Ingrédients: eau ([DE] sucre), MILCH, mystère. Peut contenir: sucre."
        ).forEach { assertUnsafe(it) }
    }

    @Test fun markerAfterUnclosedParenthesisIsUnsafe() {
        assertUnsafe("Ingrédients: eau (sucre\nDE: ZUTATEN: MILCH, mystère. Peut contenir: sucre.")
    }

    @Test fun validLanguageBoundaryStillRequiresChoosingOneList() {
        assertUnsafe("FR: Ingrédients: eau, sucre. Peut contenir: MILCH.\nDE: ZUTATEN: MILCH, mystère")
    }

    @Test fun singleListWithValidMarkerAndTraceStillAnalyzes() {
        val text = "FR: Ingrédients: eau, sucre. Peut contenir: MILCH."
        val result = analyze(text)
        assertEquals(text, result.labelSections.rawText)
        assertEquals(AnalysisVerdict.VEGAN, result.result.verdict)
        assertTrue(result.result.crossContactWarnings.any { it.contains("MILCH") })
        assertTrue(result.result.matched.none { it.id == "milk" })
    }
}

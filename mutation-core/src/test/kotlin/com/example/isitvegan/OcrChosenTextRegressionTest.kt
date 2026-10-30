package com.example.isitvegan

import org.junit.Assert.*
import org.junit.Test

class OcrChosenTextRegressionTest {
    private val service = IngredientAnalysisService(IngredientKnowledge(listOf(
        ingredient("water", "eau", VeganStatus.VEGAN),
        ingredient("sugar", "sucre", VeganStatus.VEGAN),
        ingredient("milk", "MILCH", VeganStatus.NON_VEGAN)
    )))

    private fun ingredient(id: String, text: String, status: VeganStatus) =
        Ingredient(id = id, name = text, aliases = listOf(text), status = status, reason = "test")

    private fun analyze(text: String, id: String? = null) =
        service.analyzeWithDiagnostics(text, InputMode.OCR_LABEL, UiLanguage.FR, id)

    @Test fun editedSelectedBlockWithMultipleHeadingsRequiresOneList() {
        val original = "Ingrédients: eau, sucre"
        val id = stableBlockId(original)
        val text = "$original, mystère\nZUTATEN: MILCH"
        val result = analyze(text, id)
        assertEquals(text, result.languageSegmentation.selectedText)
        assertNull(result.languageSegmentation.selectedBlockId)
        assertEquals(text, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.ambiguousOcrText)
        assertTrue(result.preprocessedInput.isEmpty())
        assertNull(result.result.verdict)
    }

    @Test fun milkNearBoundaryIsNotLost() {
        val text = "Ingrédients: eau, MILCH\nZUTATEN: sucre"
        val result = analyze(text, stableBlockId("Ingrédients: eau"))
        assertEquals(text, result.labelSections.rawText)
        assertTrue(result.ambiguousOcrText)
        assertNull(result.result.verdict)
    }

    @Test fun damagedHeadingAndAddedUnknownDoNotBecomeVegan() {
        val text = "INGREDIENTE. eau, mystère\nZUTATEN: sucre"
        val result = analyze(text, stableBlockId("Ingrédients: eau"))
        assertEquals(text, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.ambiguousOcrText)
        assertTrue(result.result.unknown.isEmpty()) // No portion of the ambiguous text is evaluated.
        assertNotEquals(AnalysisVerdict.VEGAN, result.result.verdict)
    }

    @Test fun completeTextWithMultipleListsIsNotSilentlyConfirmed() {
        val text = "Ingrédients: eau, sucre\nZUTATEN: MILCH"
        val result = analyze(text)
        assertEquals(text, result.labelSections.rawText)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.ambiguousOcrText)
        assertTrue(result.result.unknown.isEmpty())
        assertNull(result.result.verdict)
    }

    @Test fun tracesRemainOutsideVerdict() {
        val text = "Ingrédients: eau, sucre. Peut contenir: MILCH."
        val result = analyze(text, stableBlockId(text))
        assertEquals(text, result.labelSections.rawText)
        assertEquals(AnalysisVerdict.VEGAN, result.result.verdict)
        assertTrue(result.result.crossContactWarnings.any { it.contains("MILCH") })
        assertTrue(result.result.matched.none { it.id == "milk" })
    }

    @Test fun unchangedAndEditedSingleBlockKeepTheirContentsAndHonestIdentity() {
        val original = "Ingrédients: eau, sucre"
        val id = stableBlockId(original)
        val unchanged = analyze(original, id)
        assertEquals(original, unchanged.languageSegmentation.selectedText)
        assertEquals(original, unchanged.labelSections.rawText)
        assertEquals(id, unchanged.labelSections.selectedBlockId)
        assertEquals(AnalysisVerdict.VEGAN, unchanged.result.verdict)
        val edited = "$original, MILCH, mystère"
        val result = analyze(edited, id)
        assertEquals(edited, result.languageSegmentation.selectedText)
        assertNull(result.languageSegmentation.selectedBlockId)
        assertNull(result.labelSections.selectedBlockId)
        assertTrue(result.result.unknown.any { it.contains("mystère") })
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.result.verdict)
    }

    @Test fun manualInputStillUsesExistingParsing() {
        val result = service.analyzeWithDiagnostics("eau, sucre", InputMode.MANUAL_INGREDIENT_LIST)
        assertEquals(AnalysisVerdict.VEGAN, result.result.verdict)
        assertEquals(setOf("water", "sugar"), result.result.matched.map { it.id }.toSet())
    }

    @Test fun ingredientBeforeFirstRecognizedHeadingPreventsVerdict() {
        val text = "MILCH\nZUTATEN: sucre"
        val result = analyze(text, stableBlockId("ZUTATEN: sucre"))
        assertEquals(text, result.labelSections.rawText)
        assertTrue(result.ambiguousOcrText)
        assertNull(result.labelSections.selectedBlockId)
        assertNull(result.result.verdict)
    }

    @Test fun traceBetweenSelectedSectionsIsExcluded() {
        val text = "Ingrédients: eau. Peut contenir: MILCH.\nZUTATEN: sucre"
        val result = analyze(text, stableBlockId("Ingrédients: eau"))
        assertNull(result.result.verdict)
        assertTrue(result.ambiguousOcrText)
        assertTrue(result.result.crossContactWarnings.any { it.contains("MILCH") })
        assertTrue(result.result.matched.none { it.id == "milk" })
    }
}

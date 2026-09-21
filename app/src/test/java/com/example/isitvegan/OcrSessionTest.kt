package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrSessionTest {
    @Test fun rawTextStaysSeparateFromEditableText() {
        val original = "INGRÉDIENTS\neau, lait"
        val edited = OcrSession().withOcrText(original).withEditableText("INGRÉDIENTS\neau, sucre")
        assertEquals(original, edited.rawOcrText)
        assertEquals("INGRÉDIENTS\neau, sucre", edited.editableText)
        assertNotSame(edited.rawOcrText, edited.editableText)
    }

    @Test fun analysesKeepExactSubmittedSnapshots() {
        val first = AnalysisSnapshot("lait", "diagnostic lait", "NON VEGAN", 1L)
        val second = AnalysisSnapshot("eau, sucre", "diagnostic vegan", "VEGAN", 2L)
        val session = OcrSession().withOcrText("lait").withEditableText("eau, sucre")
            .addAnalysis(first).addAnalysis(second)
        assertEquals(listOf("lait", "eau, sucre"), session.analyses.map { it.submittedText })
    }

    @Test fun exportSeparatesRawEditedAndEveryResult() {
        val session = OcrSession("brut ML Kit", "texte corrigé", listOf(
            AnalysisSnapshot("brut ML Kit", "diag 1", "résultat 1", 1L),
            AnalysisSnapshot("texte corrigé", "diag 2", "résultat 2", 2L)
        ))
        val export = OcrExportReport.build(session, "0.6.0")
        assertTrue(export.indexOf("TEXTE BRUT OCR") < export.indexOf("TEXTE ÉDITABLE ACTUEL"))
        assertTrue(export.contains("brut ML Kit"))
        assertTrue(export.contains("texte corrigé"))
        assertTrue(export.contains("ANALYSE 1"))
        assertTrue(export.contains("ANALYSE 2"))
        assertTrue(export.contains("diag 1"))
        assertTrue(export.contains("diag 2"))
        assertTrue(export.contains("ancienne version"))
    }

    @Test fun editedTextIsTheValuePassedToExistingPipeline() {
        val database = listOf(
            Ingredient("milk", "lait", listOf("lait"), null, VeganStatus.NON_VEGAN, "origine animale", null),
            Ingredient("water", "eau", listOf("eau"), null, VeganStatus.VEGAN, "minéral", null)
        )
        val edited = "INGRÉDIENTS\neau"
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(edited, database, InputMode.FULL_LABEL)
        assertEquals(edited, diagnostics.input)
        assertTrue(diagnostics.result.matched.none { it.id == "milk" })
    }
}

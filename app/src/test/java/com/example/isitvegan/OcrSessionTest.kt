package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test fun reconstructedTextAndRawMlKitTextStayDistinct() {
        val result = OcrProcessingResult(
            rawText = "NL water\nFR eau",
            editableText = "FR eau\n\nNL water",
            diagnostics = OcrDiagnostics(90, 2, 2, listOf("FR", "NL"), emptyList())
        )
        val session = OcrSession().withOcrResult(result)

        assertEquals("NL water\nFR eau", session.rawOcrText)
        assertEquals("FR eau\n\nNL water", session.editableText)
        assertEquals(90, session.ocrDiagnostics?.orientationDegrees)
    }

    @Test fun exportIncludesOcrDiagnosticWithoutImageData() {
        val session = OcrSession().withOcrResult(
            OcrProcessingResult(
                "brut", "éditable",
                OcrDiagnostics(90, 8, 34, listOf("FR", "NL", "UNKNOWN"), listOf("ordre visuel incertain"))
            )
        )
        val export = OcrExportReport.build(session, "0.6.2")

        assertTrue(export.contains("Rotation EXIF transmise à ML Kit : 90°"))
        assertTrue(export.contains("Blocs géométriques ML Kit : 8"))
        assertTrue(export.contains("Lignes : 34"))
        assertTrue(export.contains("Zones détectées : FR, NL, UNKNOWN"))
        assertFalse(export.contains("data:image"))
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

    @Test fun analysisSnapshotKeepsStructuredDiagnosticsWithoutImageData() {
        val database = listOf(
            Ingredient("water", "eau", listOf("eau"), null, VeganStatus.VEGAN, "minéral", null)
        )
        val raw = "INGRÉDIENTS\n eau"
        val editable = "Ingrédients : eau"
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            editable, database, InputMode.OCR_LABEL
        )
        val session = OcrSession().withOcrResult(
            OcrProcessingResult(
                rawText = raw,
                editableText = editable,
                diagnostics = OcrDiagnostics(90, 1, 2, listOf("FR"), emptyList())
            )
        ).addAnalysis(
            AnalysisSnapshot(editable, "rapport", "VEGAN", 1L, diagnostics = diagnostics)
        )

        assertEquals(raw, session.rawOcrText)
        assertEquals(editable, session.editableText)
        assertEquals(editable, session.analyses.single().diagnostics?.input)
        assertEquals(90, session.ocrDiagnostics?.orientationDegrees)
        assertFalse(OcrExportReport.build(session, "test").contains("data:image"))
    }
}

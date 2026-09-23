package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrMultilingualSelectionTest {
    private val fixture = """
        INGREDIENTEN: water, geraffineerde ongeharde kokosolie 29%,
        zetmelen (mais, aardappel) 11%, zout, gemodificeerd tapiocazetmeel,
        zuurteregelaar (glucono-delta-lacton), aardappeleiwit,
        verdikkingsmiddel (agar-agar), natuurlijk aroma, olijfextract.

        INGREDIENTS: eau, huile de coco raffinée non hydrogénée 29%,
        amidons (mais, pomme de terre) 11%, sel, amidon de tapioca modifié,
        correcteur d’acidité (glucono-delta-lactone), protéines de pomme de terre,
        épaississant (agar-agar), arôme naturel, extrait d’olive.

        ZUTATEN: Wasser, raffiniertes ungehärtetes Kokosnussöl 29%,
        Stärke (Mais, Kartoffel), Salz, modifizierte Tapiokastärke,
        Säureregulator (Glucono-delta-lacton), Kartoffeleiweiß,
        Verdickungsmittel (Agar-Agar), natürliches Aroma, Olivenextrakt.
    """.trimIndent()

    @Test fun contentReclassifiesAmbiguousIngredientsHeadingAsFrench() {
        val segmentation = LabelLanguageSegmenter.segment(fixture, UiLanguage.FR)

        assertEquals(listOf(LabelLanguage.DUTCH, LabelLanguage.FRENCH, LabelLanguage.GERMAN),
            segmentation.blocks.map { it.language })
        assertEquals(LabelLanguage.FRENCH, segmentation.selectedLanguage)
        assertEquals("INGREDIENTS", segmentation.detectedMarker)
        assertNotNull(segmentation.blocks[1].languageCorrectionReason)
    }

    @Test fun editableTextContainsOnlySelectedBlockAndFullTextRemainsAvailable() {
        val full = OcrTextCleaner.clean(fixture)
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(full, UiLanguage.FR), full)

        assertEquals(LabelLanguage.FRENCH, selection.selectedLanguage)
        assertTrue(selection.editableText.contains("eau, huile de coco"))
        assertFalse(selection.editableText.contains("geraffineerde"))
        assertFalse(selection.editableText.contains("Wasser"))
        assertTrue(full.contains("geraffineerde"))
        assertEquals(setOf(LabelLanguage.DUTCH, LabelLanguage.FRENCH, LabelLanguage.GERMAN),
            selection.options.map { it.language }.toSet())
    }

    @Test fun analysisKeepsTheContentAssignedLanguageAndSelectedSection() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            text = fixture,
            database = emptyList(),
            inputMode = InputMode.FULL_LABEL,
            preferredLanguage = UiLanguage.FR
        )

        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertTrue(diagnostics.labelSections.ingredientsText.orEmpty().contains("eau, huile de coco"))
        assertFalse(diagnostics.labelSections.ingredientsText.orEmpty().contains("geraffineerde"))
        assertFalse(diagnostics.labelSections.ingredientsText.orEmpty().contains("Wasser"))
    }

    @Test fun selectedBlockCanBeChangedAndFullTextInvalidatesPreviousAnalysis() {
        val full = OcrTextCleaner.clean(fixture)
        val selection = OcrTextSelection.from(LabelLanguageSegmenter.segment(full, UiLanguage.FR), full)
        val result = OcrProcessingResult(
            rawText = fixture,
            editableText = selection.editableText,
            diagnostics = OcrDiagnostics(0, 3, 12, listOf("NL", "FR", "DE"), emptyList()),
            fullText = full,
            textOptions = selection.options,
            selectedOptionLanguage = selection.selectedLanguage,
            selectedOptionBlockId = selection.selectedBlockId
        )
        val analyzed = OcrSession().withOcrResult(result)
            .addAnalysis(AnalysisSnapshot("ancien", "diagnostic", "résultat", 1L))
        val german = analyzed.selectLanguage(LabelLanguage.GERMAN)
        val complete = german.addAnalysis(AnalysisSnapshot("ancien", "diagnostic", "résultat", 2L)).selectFullText()

        assertTrue(german.editableText.contains("Wasser"))
        assertTrue(german.analyses.isEmpty())
        assertEquals(german.selectedOptionBlockId, german.ocrDiagnostics?.selectedBlockId)
        assertEquals("DE", german.ocrDiagnostics?.selectedLanguage)
        assertEquals(full, complete.editableText)
        assertTrue(complete.fullTextSelected)
        assertTrue(complete.analyses.isEmpty())
        assertEquals(null, complete.ocrDiagnostics?.selectedBlockId)
    }

    @Test fun safeNoiseAndTraceBoundariesStayOutsideComposition() {
        val cleaned = OcrTextCleaner.cleanSelectedBlock("INGREDIENTS: eau, sel\nOUVRIR ICI\nImportateur: Exemple")
        val sections = LabelSectionExtractor.extract(
            LabelLanguage.FRENCH,
            "Ingrédients: eau, sel. Peut contenir lait. Importateur: Exemple"
        )

        assertFalse(cleaned.contains("OUVRIR ICI"))
        assertEquals("eau, sel.", sections.ingredientsText?.trim())
        assertTrue(sections.tracesText.orEmpty().contains("Peut contenir lait"))
        assertFalse(sections.tracesText.orEmpty().contains("Importateur"))
    }

    @Test fun nonVeganBlockerStillWinsOverUnknownsAndTraces() {
        val nonVegan = Ingredient(
            id = "gelatin",
            name = "gélatine",
            aliases = listOf("gélatine"),
            status = VeganStatus.NON_VEGAN,
            reason = "animal"
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, VerdictEngine.evaluate(listOf(nonVegan), listOf("mystère")))
        assertEquals(AnalysisVerdict.UNCERTAIN,
            VerdictEngine.evaluate(
                listOf(
                    Ingredient(
                        id = "x",
                        name = "x",
                        aliases = listOf("x"),
                        status = VeganStatus.UNCERTAIN,
                        reason = "incertain"
                    )
                ),
                emptyList()
            )
        )
        assertEquals(AnalysisVerdict.INCONCLUSIVE, VerdictEngine.evaluate(emptyList(), listOf("mystère")))
    }
}

package com.example.isitvegan

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Real submitted text, not a reference transcription or a test of ML Kit acquisition. */
class B2RealOcrExportRegressionTest {
    private val service = IngredientAnalysisService(IngredientKnowledge.fromJson(
        File("src/main/assets/ingredients.json").readText(),
        File("src/main/assets/ingredient_aliases_multilingual.json").readText(),
        File("src/main/assets/origin_qualifier_rules.json").readText()
    ))

    private fun fixture(number: Int): String = javaClass.getResource(
        "/ocr/real-export-0.6.13.13/analysis-$number.txt"
    )!!.readText(Charsets.UTF_8).removeSuffix("\n").removeSuffix("\r")

    private fun analyze(number: Int): AnalysisDiagnostics {
        val text = fixture(number)
        val d = service.analyzeWithDiagnostics(text, InputMode.OCR_LABEL)
        println("B2 real export analysis=$number availability=${d.result.availability} ambiguous=${d.ambiguousOcrText} " +
            "blocks=${d.languageSegmentation.blocks.map { Triple(it.id, it.segmentId, it.selectionScore) }} " +
            "selected=${d.languageSegmentation.selectedSegmentId} verdict=${d.result.verdict} " +
            "conditional=${d.verdictExplanation.conditionalVerdict}/${d.verdictExplanation.conditionalReason} " +
            "unknowns=${d.visibleUnknownIngredients.size} traces=${d.crossContactWarnings.size}")
        assertEquals(text, d.input)
        assertEquals(1, d.languageSegmentation.blocks.size)
        val block = d.languageSegmentation.blocks.single()
        assertEquals(if (number == 1) "block-613b6729" else "block-603e80ab", block.id)
        assertEquals(if (number == 1) 36 else 89, block.selectionScore)
        assertEquals("segment-0-681", block.segmentId)
        assertEquals(if (number == 1) null else block.segmentId, d.languageSegmentation.selectedSegmentId)
        assertEquals("FRENCH", d.languageSegmentation.blocks.single().language.name)
        assertTrue(d.input.endsWith("UND UBERZUG AUS KARAMELLGEBÃCK"))
        assertEquals(listOf("Peut contenir : de larachlde, des fruitsà coque et du ait."), d.crossContactWarnings)
        assertFalse(d.preprocessedInput.contains("HAFERRIEGEL"))
        assertFalse(d.result.matched.any { it.id == "milk" })
        return d
    }

    @Test fun exactSubmittedTextsKeepBothOcrDifferences() {
        assertEquals(681, fixture(1).length)
        assertEquals(681, fixture(2).length)
        assertEquals(fixture(1).replace("(arine de blé", "farine de blé"), fixture(2))
        assertTrue(fixture(1).contains("(58%) (arine de blé"))
        assertTrue(fixture(2).contains("(58%) farine de blé"))
    }

    @Test fun suppliedRawTextHasFiveOptionsAndManualFrenchChoiceKeepsInitialItalianReason() {
        val raw = javaClass.getResource("/ocr/real-export-0.6.13.13/raw-ocr.txt")!!
            .readText(Charsets.UTF_8).removeSuffix("\n").removeSuffix("\r")
        // Pure post-OCR text pipeline only: no image, geometry, EXIF or ML Kit execution.
        val cleaned = OcrTextCleaner.clean(raw)
        val segmentation = LabelLanguageSegmenter.segment(cleaned, UiLanguage.FR)
        val selection = OcrTextSelection.from(segmentation, cleaned)
        println("B2 raw text blocks=${segmentation.blocks.map { Triple(it.language, it.selectionScore, it.segmentId) }} " +
            "auto=${selection.selectedLanguage} reason=${segmentation.selectionReason}")
        assertEquals(5, segmentation.blocks.size)
        assertEquals(5, selection.options.size)
        assertEquals(LabelLanguage.ITALIAN, selection.selectedLanguage)
        assertEquals(88, segmentation.blocks.single { it.language == LabelLanguage.ITALIAN }.selectionScore)
        val diagnostics = selection.updateDiagnostics(OcrDiagnostics(
            orientationDegrees = null, blockCount = 0, lineCount = 0,
            detectedZones = segmentation.blocks.map { it.language.displayName }, warnings = emptyList(),
            selectedLanguage = selection.selectedLanguage?.displayName,
            selectionReason = segmentation.selectionReason, selectedBlockId = selection.selectedBlockId,
            languageSegmentCount = segmentation.blocks.size
        ))
        val initial = OcrSession(
            rawOcrText = raw, fullOcrText = cleaned, editableText = selection.editableText,
            textOptions = selection.options, selectedOptionLanguage = selection.selectedLanguage,
            selectedOptionBlockId = selection.selectedBlockId, selectedOptionSegmentId = selection.selectedSegmentId,
            ocrDiagnostics = diagnostics
        )
        val french = selection.options.single { it.language == LabelLanguage.FRENCH }
        val selected = initial.selectOption(french.segmentId)
        assertEquals(french.text, selected.editableText)
        assertEquals(fixture(1), selected.editableText)
        assertEquals(LabelLanguage.FRENCH, selected.selectedOptionLanguage)
        assertEquals("FR", selected.ocrDiagnostics?.selectedLanguage)
        assertEquals(french.segmentId, selected.ocrDiagnostics?.selectedSegmentId)
        assertEquals(segmentation.selectionReason, selected.ocrDiagnostics?.selectionReason)
        assertTrue(selected.ocrDiagnostics?.selectionReason.orEmpty().contains("EN vers IT"))
        assertEquals(5, selected.ocrDiagnostics?.selectableBlockCount)
        assertEquals(1, selected.ocrDiagnostics?.selectedBlockCount)
        assertEquals(4, selected.ocrDiagnostics?.rejectedBlockCount)
        assertTrue(selected.editableText.endsWith("UND UBERZUG AUS KARAMELLGEBÃCK"))
    }

    @Test fun firstSubmittedTextRemainsUnevaluatedWithAmbiguousStructure() {
        val d = analyze(1)
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, d.result.availability)
        assertTrue(d.ambiguousOcrText)
        assertTrue(d.languageSegmentation.blocks.single().manifestlyTruncated)
        assertTrue(d.preprocessedInput.isEmpty())
        assertTrue(d.result.matched.isEmpty())
        assertNull(d.verdictExplanation.conditionalVerdict)
    }

    @Test fun sourceContentIdIsClearedByRefusalOrChangedTextButCurrentIdCanBeRetained() {
        val refused = service.analyzeWithDiagnostics(
            fixture(1), InputMode.OCR_LABEL, selectedBlockId = "block-613b6729"
        )
        assertTrue(refused.ambiguousOcrText)
        assertNull(refused.labelSections.selectedBlockId)
        val editedWithOriginalId = service.analyzeWithDiagnostics(
            fixture(2), InputMode.OCR_LABEL, selectedBlockId = "block-613b6729"
        )
        assertFalse(editedWithOriginalId.ambiguousOcrText)
        assertNull(editedWithOriginalId.labelSections.selectedBlockId)
        val editedWithCurrentId = service.analyzeWithDiagnostics(
            fixture(2), InputMode.OCR_LABEL, selectedBlockId = "block-603e80ab"
        )
        assertFalse(editedWithCurrentId.ambiguousOcrText)
        assertEquals("block-603e80ab", editedWithCurrentId.labelSections.selectedBlockId)
        assertEquals(editedWithOriginalId.result, editedWithCurrentId.result)
    }

    @Test fun secondSubmittedTextKeepsUnknownsUncertaintyAndTracesSeparate() {
        val d = analyze(2)
        assertEquals(AnalysisAvailability.INGREDIENT_LIST_ANALYZED, d.result.availability)
        assertFalse(d.ambiguousOcrText)
        assertFalse(d.languageSegmentation.blocks.single().manifestlyTruncated)
        assertEquals(AnalysisVerdict.UNCERTAIN, d.result.verdict)
        assertTrue(d.result.matched.any { it.id == "natural_flavouring" })
        assertTrue(d.visibleUnknownIngredients.isNotEmpty())
        assertTrue(d.visibleUnknownIngredients.any { it.contains("256") })
        assertEquals(setOf("wheat_flour", "sugar", "vegetable_oil", "salt", "palm_oil", "soy", "natural_flavouring"),
            d.result.matched.map { it.id }.toSet())
        val exportedUnknowns = "256 | biscults caramelises | sirop de candi | carbonate adde de sodum | farne desoja | cannelle | sue | goja | 196 | sirop | deriz | solats de proteines de | amidon de tapioca | se | huiles | colza | palme\" | farine de | şure | cassa | palime | Ssue de plantations durables et certfiées"
        assertEquals(exportedUnknowns.split(" | ").toSet(), d.visibleUnknownIngredients.toSet())
        assertNull(d.verdictExplanation.conditionalVerdict)
        assertEquals(ConditionalVerdictReason.UNKNOWN_INGREDIENT_REMAINS, d.verdictExplanation.conditionalReason)
        assertEquals(AnalysisVerdict.VEGAN, d.verdictExplanation.establishedIngredientVerdict)
        assertTrue(d.preprocessedInput.contains("Ssue de plantations durables et certfiées"))
    }
}



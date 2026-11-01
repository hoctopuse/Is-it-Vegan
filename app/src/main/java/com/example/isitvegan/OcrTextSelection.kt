package com.example.isitvegan

internal data class OcrTextSelectionResult(
    val editableText: String,
    val options: List<OcrTextOption>,
    val selectedLanguage: LabelLanguage?,
    val selectedBlockId: String?,
    val selectedSegmentId: String?
) {
    /** Selectable = displayed options; selected = segment placed in the editor (even if filtered).
     * Rejected = other detected segments; full-text mode selects/rejects no individual segment.
     * None of these counts establishes that the edited text is analysable. */
    fun updateDiagnostics(diagnostics: OcrDiagnostics): OcrDiagnostics = diagnostics.copy(
        selectableBlockCount = options.size,
        selectedSegmentId = selectedSegmentId,
        selectedBlockCount = if (selectedSegmentId == null) 0 else 1,
        rejectedBlockCount = if (selectedSegmentId == null) 0 else (diagnostics.languageSegmentCount - 1).coerceAtLeast(0)
    )
}

/** Pure selection step shared by OCR processing and JVM regression tests. */
internal object OcrTextSelection {
    private val selectableLanguages = setOf(
        LabelLanguage.FRENCH,
        LabelLanguage.ENGLISH,
        LabelLanguage.DUTCH,
        LabelLanguage.GERMAN,
        LabelLanguage.ITALIAN,
        LabelLanguage.SPANISH,
        LabelLanguage.POLISH
    )

    fun from(segmentation: LanguageSegmentation, fullText: String): OcrTextSelectionResult {
        val options = segmentation.blocks
            .filter { it.hasIngredientHeading && it.language in selectableLanguages && it.headingConfidence == HeadingConfidence.CONFIRMED }
            .map { block ->
                OcrTextOption(
                    language = block.language,
                    text = OcrTextCleaner.cleanSelectedBlock(block.rawText),
                    marker = block.detectedMarker,
                    languageCorrectionReason = block.languageCorrectionReason,
                    blockId = block.id,
                    segmentId = block.segmentId,
                    headingConfidence = block.headingConfidence
                )
            }
        val selectedOption = if (segmentation.usedFallback) {
            null
        } else {
            options.singleOrNull { it.segmentId == segmentation.selectedSegmentId }
        }
        val editable = if (segmentation.usedFallback) {
            fullText
        } else {
            selectedOption?.text
                ?: OcrTextCleaner.cleanSelectedBlock(segmentation.selectedText)
        }
        return OcrTextSelectionResult(
            editableText = editable,
            options = options,
            selectedLanguage = segmentation.selectedLanguage.takeUnless { segmentation.usedFallback },
            selectedBlockId = segmentation.selectedBlockId.takeUnless { segmentation.usedFallback },
            selectedSegmentId = segmentation.selectedSegmentId.takeUnless { segmentation.usedFallback }
        )
    }
}

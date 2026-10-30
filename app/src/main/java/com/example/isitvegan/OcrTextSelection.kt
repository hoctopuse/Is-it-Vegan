package com.example.isitvegan

internal data class OcrTextSelectionResult(
    val editableText: String,
    val options: List<OcrTextOption>,
    val selectedLanguage: LabelLanguage?,
    val selectedBlockId: String?
)

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
            .groupBy { it.language }
            .values
            .map { languageBlocks ->
                languageBlocks.firstOrNull { it.headingConfidence == HeadingConfidence.CONFIRMED } ?: languageBlocks.first()
            }
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
        val editable = if (segmentation.usedFallback) {
            fullText
        } else {
            options.firstOrNull { it.blockId == segmentation.selectedBlockId }?.text
                ?: OcrTextCleaner.cleanSelectedBlock(segmentation.selectedText)
        }
        return OcrTextSelectionResult(
            editableText = editable,
            options = options,
            selectedLanguage = segmentation.selectedLanguage.takeUnless { segmentation.usedFallback },
            selectedBlockId = segmentation.selectedBlockId.takeUnless { segmentation.usedFallback }
        )
    }
}

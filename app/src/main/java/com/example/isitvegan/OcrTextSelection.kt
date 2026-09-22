package com.example.isitvegan

internal data class OcrTextSelectionResult(
    val editableText: String,
    val options: List<OcrTextOption>,
    val selectedLanguage: LabelLanguage?
)

/** Pure selection step shared by OCR processing and JVM regression tests. */
internal object OcrTextSelection {
    private val selectableLanguages = setOf(
        LabelLanguage.FRENCH,
        LabelLanguage.ENGLISH,
        LabelLanguage.DUTCH,
        LabelLanguage.GERMAN
    )

    fun from(segmentation: LanguageSegmentation, fullText: String): OcrTextSelectionResult {
        val options = segmentation.blocks
            .filter { it.hasIngredientHeading && it.language in selectableLanguages }
            .distinctBy { it.language }
            .map { block ->
                OcrTextOption(
                    language = block.language,
                    text = OcrTextCleaner.cleanSelectedBlock(block.rawText),
                    marker = block.detectedMarker,
                    languageCorrectionReason = block.languageCorrectionReason
                )
            }
        val editable = if (segmentation.usedFallback) {
            fullText
        } else {
            options.firstOrNull { it.language == segmentation.selectedLanguage }?.text
                ?: OcrTextCleaner.cleanSelectedBlock(segmentation.selectedText)
        }
        return OcrTextSelectionResult(
            editableText = editable,
            options = options,
            selectedLanguage = segmentation.selectedLanguage.takeUnless { segmentation.usedFallback }
        )
    }
}

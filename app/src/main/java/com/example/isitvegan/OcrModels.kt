package com.example.isitvegan

internal data class OcrBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val height: Int get() = (bottom - top).coerceAtLeast(0)

    fun verticallyOverlaps(other: OcrBounds): Boolean =
        minOf(bottom, other.bottom) > maxOf(top, other.top)
}

internal data class OcrElement(
    val text: String,
    val bounds: OcrBounds?,
    val blockNumber: Int,
    val lineNumber: Int,
    val elementOrder: Int
)

internal data class OcrLine(
    val text: String,
    val bounds: OcrBounds?,
    val blockNumber: Int,
    val lineNumber: Int,
    val mlKitOrder: Int,
    val elements: List<OcrElement> = emptyList()
)

internal data class OcrBlock(
    val blockNumber: Int,
    val mlKitOrder: Int,
    val bounds: OcrBounds?,
    val lines: List<OcrLine>
)

internal data class OcrDocument(
    val rawText: String,
    val orientationDegrees: Int?,
    val blocks: List<OcrBlock>
) {
    val lineCount: Int get() = blocks.sumOf { it.lines.size }
}

internal data class OcrReconstruction(
    val text: String,
    val warnings: List<String>
)

internal data class OcrDiagnostics(
    val orientationDegrees: Int?,
    val blockCount: Int,
    val lineCount: Int,
    val detectedZones: List<String>,
    val warnings: List<String>,
    val selectedLanguage: String? = null,
    val languagePreference: List<String> = emptyList(),
    val selectionReason: String? = null,
    val detectedBlockDetails: List<String> = emptyList()
)

internal data class OcrTextOption(
    val language: LabelLanguage,
    val text: String,
    val marker: String?,
    val languageCorrectionReason: String? = null
)

internal data class OcrProcessingResult(
    val rawText: String,
    val editableText: String,
    val diagnostics: OcrDiagnostics,
    val usedRawFallback: Boolean = false,
    val fullText: String = editableText,
    val textOptions: List<OcrTextOption> = emptyList(),
    val selectedOptionLanguage: LabelLanguage? = null
)

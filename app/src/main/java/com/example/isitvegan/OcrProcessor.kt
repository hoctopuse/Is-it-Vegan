package com.example.isitvegan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

internal class OcrProcessor(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageFactory = OcrImageInputFactory(context)

    fun process(uri: Uri, onSuccess: (OcrProcessingResult) -> Unit, onFailure: (String) -> Unit) {
        val prepared = try {
            imageFactory.create(uri)
        } catch (_: IOException) {
            onFailure("Cette image est inaccessible ou n’est pas prise en charge.")
            return
        } catch (_: RuntimeException) {
            onFailure("Impossible de lire cette image.")
            return
        }
        recognizer.process(prepared.image)
            .addOnSuccessListener { recognized ->
                val result = try {
                    val document = recognized.toOcrDocument(prepared.orientationDegrees)
                    val reconstruction = OcrTextReconstructor.reconstruct(document)
                    val segmentation = LabelLanguageSegmenter.segment(reconstruction.text)
                    val editable = OcrTextCleaner.clean(reconstruction.text)
                    OcrProcessingResult(
                        rawText = recognized.text,
                        editableText = editable,
                        diagnostics = OcrDiagnostics(
                            orientationDegrees = prepared.orientationDegrees,
                            blockCount = document.blocks.size,
                            lineCount = document.lineCount,
                            detectedZones = segmentation.blocks.map {
                                if (it.language == LabelLanguage.UNKNOWN) "UNKNOWN" else it.language.displayName
                            }.distinct(),
                            warnings = (prepared.warnings + reconstruction.warnings).distinct()
                        )
                    )
                } catch (_: RuntimeException) {
                    OcrProcessingResult(
                        rawText = recognized.text,
                        editableText = recognized.text,
                        diagnostics = OcrDiagnostics(
                            orientationDegrees = prepared.orientationDegrees,
                            blockCount = recognized.textBlocks.size,
                            lineCount = recognized.textBlocks.sumOf { it.lines.size },
                            detectedZones = listOf("UNKNOWN"),
                            warnings = (prepared.warnings +
                                "post-traitement OCR impossible ; texte brut conservé").distinct()
                        ),
                        usedRawFallback = true
                    )
                }
                onSuccess(result)
            }
            .addOnFailureListener { onFailure("L’extraction OCR a échoué. Vous pouvez saisir le texte manuellement.") }
            .addOnCompleteListener { prepared.bitmapToRecycle?.recycle() }
    }

    fun close() = recognizer.close()

    private fun Text.toOcrDocument(orientationDegrees: Int?): OcrDocument {
        var lineOrder = 0
        val convertedBlocks = textBlocks.mapIndexed { blockIndex, block ->
            val lines = block.lines.mapIndexed { lineIndex, line ->
                OcrLine(
                    text = line.text,
                    bounds = line.boundingBox?.toOcrBounds(),
                    blockNumber = blockIndex,
                    lineNumber = lineIndex,
                    mlKitOrder = lineOrder++,
                    elements = line.elements.mapIndexed { elementIndex, element ->
                        OcrElement(
                            text = element.text,
                            bounds = element.boundingBox?.toOcrBounds(),
                            blockNumber = blockIndex,
                            lineNumber = lineIndex,
                            elementOrder = elementIndex
                        )
                    }
                )
            }
            OcrBlock(blockIndex, blockIndex, block.boundingBox?.toOcrBounds(), lines)
        }
        return OcrDocument(text, orientationDegrees, convertedBlocks)
    }

    private fun android.graphics.Rect.toOcrBounds() = OcrBounds(left, top, right, bottom)
}

package com.example.isitvegan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

internal class OcrProcessor(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageFactory = OcrImageInputFactory(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun process(uri: Uri, onSuccess: (OcrProcessingResult) -> Unit, onFailure: (String) -> Unit, preferredLanguage: UiLanguage = UiLanguage.FR) {
        process(uri, null, onSuccess, onFailure, preferredLanguage)
    }

    fun process(
        uri: Uri,
        cropRect: OcrCropRect?,
        onSuccess: (OcrProcessingResult) -> Unit,
        onFailure: (String) -> Unit,
        preferredLanguage: UiLanguage = UiLanguage.FR
    ) {
        scope.launch {
            val prepared = try {
                if (cropRect == null) imageFactory.create(uri) else prepareCrop(uri, cropRect)
            } catch (_: IOException) {
                deliverFailure(onFailure, "Cette image est inaccessible ou n’est pas prise en charge.")
                return@launch
            } catch (_: RuntimeException) {
                deliverFailure(onFailure, "Impossible de lire cette image.")
                return@launch
            } catch (_: OutOfMemoryError) {
                deliverFailure(onFailure, "Image trop grande pour cet appareil. Essayez un recadrage plus serré.")
                return@launch
            }
            processPrepared(prepared, onSuccess, onFailure, preferredLanguage)
        }
    }

    private fun prepareCrop(uri: Uri, cropRect: OcrCropRect): PreparedOcrImage {
        val source = imageFactory.decodeForCrop(uri)
        var crop: Bitmap? = null
        try {
            crop = OcrBitmapCropper.crop(source, cropRect)
            if (crop !== source) source.recycle()
            return PreparedOcrImage(
                image = InputImage.fromBitmap(crop, 0),
                orientationDegrees = 0,
                bitmapToRecycle = crop,
                warnings = listOf("zone recadrée appliquée après orientation EXIF")
            )
        } catch (error: Throwable) {
            crop?.takeUnless(Bitmap::isRecycled)?.recycle()
            if (crop !== source && !source.isRecycled) source.recycle()
            throw error
        }
    }

    /** Runs the same ML Kit pipeline on an in-memory, already oriented crop. */
    fun process(bitmap: Bitmap, onSuccess: (OcrProcessingResult) -> Unit, onFailure: (String) -> Unit, preferredLanguage: UiLanguage = UiLanguage.FR) {
        scope.launch {
            processPrepared(
                PreparedOcrImage(InputImage.fromBitmap(bitmap, 0), orientationDegrees = 0),
                onSuccess,
                onFailure,
                preferredLanguage
            )
        }
    }

    private fun processPrepared(
        prepared: PreparedOcrImage,
        onSuccess: (OcrProcessingResult) -> Unit,
        onFailure: (String) -> Unit,
        preferredLanguage: UiLanguage
    ) {
        val task = try {
            recognizer.process(prepared.image)
        } catch (_: RuntimeException) {
            prepared.bitmapToRecycle?.recycle()
            scope.launch { deliverFailure(onFailure, "L’extraction OCR a échoué. Vous pouvez saisir le texte manuellement.") }
            return
        }
        task
            .addOnSuccessListener { recognized ->
                scope.launch(Dispatchers.Default) {
                    val result = try {
                        val document = recognized.toOcrDocument(prepared.orientationDegrees)
                        val reconstruction = OcrTextReconstructor.reconstruct(document)
                        // ML Kit already returns its best reading order in Text.text. Re-sorting every
                        // block from bounding boxes can scramble curved, rotated or multi-column labels
                        // even though recognition itself succeeded. Keep the native order as the source
                        // of the editable/analyzed text; retain geometric reconstruction for diagnostics.
                        val fullEditable = OcrTextCleaner.clean(recognized.text)
                        val reconstructed = OcrTextCleaner.clean(reconstruction.text)
                        val nativeOrderWarning = if (reconstructed != fullEditable) {
                            listOf("ordre natif ML Kit conservé ; reconstruction géométrique ignorée")
                        } else {
                            emptyList()
                        }
                        val segmentation = LabelLanguageSegmenter.segment(fullEditable, preferredLanguage)
                        val textSelection = OcrTextSelection.from(segmentation, fullEditable)
                        OcrProcessingResult(
                            rawText = recognized.text,
                            editableText = textSelection.editableText,
                            diagnostics = OcrDiagnostics(
                                orientationDegrees = prepared.orientationDegrees,
                                blockCount = document.blocks.size,
                                lineCount = document.lineCount,
                                detectedZones = segmentation.blocks.map {
                                    if (it.language == LabelLanguage.UNKNOWN) "UNKNOWN" else it.language.displayName
                                }.distinct(),
                                warnings = (prepared.warnings + nativeOrderWarning).distinct(),
                                selectedLanguage = segmentation.selectedLanguage.displayName,
                                languagePreference = languagePreference(preferredLanguage),
                                selectionReason = segmentation.selectionReason,
                                selectedBlockId = textSelection.selectedBlockId,
                                languageSegmentCount = segmentation.blocks.size,
                                selectableBlockCount = segmentation.blocks.size,
                                selectedBlockCount = if (segmentation.selectedBlockId == null) 0 else 1,
                                rejectedBlockCount = segmentation.blocks.count { it.id != segmentation.selectedBlockId },
                                detectedBlockDetails = segmentation.blocks.map { block ->
                                    buildString {
                                        append("${block.id} [${block.segmentId}]: ${block.detectedMarker ?: "sans marqueur"} → ${block.language.displayName}; score=${block.selectionScore}")
                                        block.languageCorrectionReason?.let { append(" ($it)") }
                                    }
                                }
                            ),
                            fullText = fullEditable,
                            textOptions = textSelection.options,
                            selectedOptionLanguage = textSelection.selectedLanguage,
                            selectedOptionBlockId = textSelection.selectedBlockId
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
                    withContext(Dispatchers.Main) { onSuccess(result) }
                }
            }
            .addOnFailureListener {
                scope.launch { deliverFailure(onFailure, "L’extraction OCR a échoué. Vous pouvez saisir le texte manuellement.") }
            }
            .addOnCompleteListener {
                prepared.bitmapToRecycle?.takeUnless(Bitmap::isRecycled)?.recycle()
            }
    }

    private fun languagePreference(language: UiLanguage): List<String> = when (language) {
        UiLanguage.FR -> listOf("FR", "EN", "NL")
        UiLanguage.EN -> listOf("EN", "FR", "NL")
        UiLanguage.NL -> listOf("NL", "EN", "FR")
    }

    private suspend fun deliverFailure(onFailure: (String) -> Unit, message: String) {
        withContext(Dispatchers.Main) { onFailure(message) }
    }

    fun close() {
        scope.cancel()
        recognizer.close()
    }

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

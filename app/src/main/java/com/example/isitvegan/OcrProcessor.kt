package com.example.isitvegan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class OcrProcessor(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val appContext = context.applicationContext

    fun process(uri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val image = try {
            InputImage.fromFilePath(appContext, uri)
        } catch (_: IOException) {
            onFailure("Cette image est inaccessible ou n’est pas prise en charge.")
            return
        } catch (_: RuntimeException) {
            onFailure("Impossible de lire cette image.")
            return
        }
        recognizer.process(image)
            .addOnSuccessListener { text -> onSuccess(text.text) }
            .addOnFailureListener { onFailure("L’extraction OCR a échoué. Vous pouvez saisir le texte manuellement.") }
    }

    fun close() = recognizer.close()
}

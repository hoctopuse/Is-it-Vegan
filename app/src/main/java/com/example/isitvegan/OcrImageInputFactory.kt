package com.example.isitvegan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

internal data class PreparedOcrImage(
    val image: InputImage,
    val orientationDegrees: Int?,
    val bitmapToRecycle: Bitmap? = null,
    val warnings: List<String> = emptyList()
)

/** Reads EXIF once and supplies ML Kit with the matching rotation without JPEG recompression. */
internal class OcrImageInputFactory(context: Context) {
    private val appContext = context.applicationContext

    @Throws(IOException::class)
    fun create(uri: Uri): PreparedOcrImage {
        val orientation = readExifOrientation(uri)
        val rotation = orientation?.let(::rotationDegrees)
        val mirrored = orientation in mirroredOrientations
        if (rotation == null || mirrored) {
            val warning = if (mirrored) {
                listOf("orientation EXIF miroir laissée au chargeur ML Kit")
            } else {
                listOf("orientation EXIF indéterminée ; comportement ML Kit conservé")
            }
            return PreparedOcrImage(InputImage.fromFilePath(appContext, uri), rotation, warnings = warning)
        }

        val bitmap = appContext.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            ?: throw IOException("Image illisible")
        return PreparedOcrImage(InputImage.fromBitmap(bitmap, rotation), rotation, bitmap)
    }

    private fun readExifOrientation(uri: Uri): Int? = try {
        appContext.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            if (!exif.hasAttribute(ExifInterface.TAG_ORIENTATION)) null
            else exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                .takeUnless { it == ExifInterface.ORIENTATION_UNDEFINED }
        }
    } catch (_: IOException) {
        null
    } catch (_: RuntimeException) {
        null
    }

    private fun rotationDegrees(orientation: Int): Int? = when (orientation) {
        ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> 0
        ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
        ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
        ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
        else -> null
    }

    private companion object {
        val mirroredOrientations = setOf(
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE
        )
    }
}

package com.example.isitvegan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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
        val bitmap = decodeScaledBitmap(uri, OCR_MAX_DIMENSION)

        if (mirrored) {
            val oriented = applyExifOrientation(bitmap, orientation)
            return PreparedOcrImage(
                InputImage.fromBitmap(oriented, 0),
                rotation,
                oriented,
                listOf("orientation EXIF miroir appliquée avant ML Kit")
            )
        }

        return PreparedOcrImage(
            InputImage.fromBitmap(bitmap, rotation ?: 0),
            rotation,
            bitmap,
            if (rotation == null) listOf("orientation EXIF indéterminée ; rotation 0° conservée") else emptyList()
        )
    }

    /** Decodes the image in the visual orientation used by the crop editor. */
    @Throws(IOException::class)
    fun decodeForPreview(uri: Uri): Bitmap {
        val orientation = readExifOrientation(uri)
        return applyExifOrientation(decodeScaledBitmap(uri, PREVIEW_MAX_DIMENSION), orientation)
    }

    /** Decodes a bounded, visually oriented source used to create the in-memory OCR crop. */
    @Throws(IOException::class)
    fun decodeForCrop(uri: Uri): Bitmap {
        val orientation = readExifOrientation(uri)
        return applyExifOrientation(decodeScaledBitmap(uri, OCR_MAX_DIMENSION), orientation)
    }

    private fun decodeScaledBitmap(uri: Uri, maxDimension: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = appContext.contentResolver.openInputStream(uri)
            ?: throw IOException("Image illisible")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Dimensions d’image invalides")

        val sampleSize = OcrImageSizing.sampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = if (maxDimension == PREVIEW_MAX_DIMENSION) {
                Bitmap.Config.RGB_565
            } else {
                Bitmap.Config.ARGB_8888
            }
        }
        val decoded = appContext.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("Image illisible")

        val largest = maxOf(decoded.width, decoded.height)
        if (largest <= maxDimension) return decoded
        val scale = maxDimension.toFloat() / largest
        return Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true
        ).also { scaled -> if (scaled !== decoded) decoded.recycle() }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int?): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
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
        // A 720p-class phone does not benefit from retaining a 1280 px preview.
        // OCR keeps more detail, while 2048 px bounds the transient bitmap near 16 MiB.
        const val PREVIEW_MAX_DIMENSION = 1024
        const val OCR_MAX_DIMENSION = 2048

        val mirroredOrientations = setOf(
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE
        )
    }
}

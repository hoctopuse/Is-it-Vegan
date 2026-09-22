package com.example.isitvegan

import android.graphics.Bitmap

/** Pure bitmap operation. Callers must run it away from the main thread. */
internal object OcrBitmapCropper {
    fun crop(bitmap: Bitmap, rect: OcrCropRect): Bitmap {
        val safe = OcrCropGeometry.clamp(rect)
        val left = (safe.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val top = (safe.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val right = (safe.right * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = (safe.bottom * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }
}

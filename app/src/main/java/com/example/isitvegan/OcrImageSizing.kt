package com.example.isitvegan

/** Keeps decoded photos bounded without dropping much below the requested OCR size. */
internal object OcrImageSizing {
    fun sampleSize(width: Int, height: Int, maxDimension: Int): Int {
        require(width > 0 && height > 0 && maxDimension > 0)
        val largest = maxOf(width, height)
        val minimumDecodedDimension = maxDimension * 3 / 4
        var sampleSize = 1
        while (largest / (sampleSize * 2) >= minimumDecodedDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }
}

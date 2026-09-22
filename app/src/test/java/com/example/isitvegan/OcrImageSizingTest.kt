package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrImageSizingTest {
    @Test fun phonePhotoIsDecodedNearTheOcrTargetWithoutFullResolutionAllocation() {
        assertEquals(4, OcrImageSizing.sampleSize(8160, 6120, 2048))
    }

    @Test fun moderateImageKeepsEnoughDetailForSmallText() {
        assertEquals(1, OcrImageSizing.sampleSize(3000, 2000, 2048))
    }

    @Test fun previewUsesAProgressivelySmallerDecode() {
        assertEquals(8, OcrImageSizing.sampleSize(8160, 6120, 1024))
    }
}

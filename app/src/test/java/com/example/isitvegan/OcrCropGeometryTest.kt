package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrCropGeometryTest {
    private val viewport = OcrImageViewport(0f, 0f, 1000f, 500f)
    private val rect = OcrCropRect(.2f, .2f, .8f, .8f)

    @Test fun clampKeepsCropInsideImageAndAboveMinimumSize() {
        val clamped = OcrCropGeometry.clamp(OcrCropRect(-.4f, .9f, .05f, 1.4f))
        assertEquals(0f, clamped.left, 0.001f)
        assertEquals(0.88f, clamped.top, 0.001f)
        assertTrue(clamped.width >= OcrCropGeometry.MIN_SIZE)
        assertTrue(clamped.height >= OcrCropGeometry.MIN_SIZE)
        assertTrue(clamped.right <= 1f && clamped.bottom <= 1f)
    }

    @Test fun movingCropClampsToImageBounds() {
        val moved = OcrCropGeometry.move(OcrCropRect(.2f, .2f, .7f, .7f), .6f, -.5f)
        assertEquals(.5f, moved.left, 0.001f)
        assertEquals(0f, moved.top, 0.001f)
        assertEquals(1f, moved.right, 0.001f)
        assertEquals(.5f, moved.bottom, 0.001f)
    }

    @Test fun fullCropRepresentsResetState() {
        assertEquals(OcrCropRect.full(), OcrCropGeometry.clamp(OcrCropRect.full()))
    }

    @Test fun touchingExactlyOnHandleSelectsCorner() {
        val gesture = OcrCropGeometry.hitTest(OcrPoint(200f, 100f), viewport, rect, 32f, 20f)
        assertEquals(OcrCropGesture.TOP_LEFT, gesture)
    }

    @Test fun touchingNearHandleInsideInvisibleHitboxSelectsCorner() {
        val gesture = OcrCropGeometry.hitTest(OcrPoint(225f, 124f), viewport, rect, 32f, 20f)
        assertEquals(OcrCropGesture.TOP_LEFT, gesture)
    }

    @Test fun cornerHasPriorityOverNearbyEdge() {
        val gesture = OcrCropGeometry.hitTest(OcrPoint(200f, 118f), viewport, rect, 32f, 20f)
        assertEquals(OcrCropGesture.TOP_LEFT, gesture)
    }

    @Test fun edgeCanBeSelectedAndMovedIndependently() {
        val gesture = OcrCropGeometry.hitTest(OcrPoint(205f, 250f), viewport, rect, 32f, 20f)
        val resized = OcrCropGeometry.resize(rect, gesture, .1f, 0f)
        assertEquals(OcrCropGesture.LEFT, gesture)
        assertEquals(.3f, resized.left, .001f)
        assertEquals(.8f, resized.right, .001f)
        assertEquals(rect.top, resized.top, .001f)
        assertEquals(rect.bottom, resized.bottom, .001f)
    }

    @Test fun touchingInsideMovesWholeFrame() {
        val gesture = OcrCropGeometry.hitTest(OcrPoint(500f, 250f), viewport, rect, 32f, 20f)
        val moved = OcrCropGeometry.resize(rect, gesture, .1f, -.1f)
        assertEquals(OcrCropGesture.MOVE, gesture)
        assertEquals(.3f, moved.left, .001f)
        assertEquals(.1f, moved.top, .001f)
        assertEquals(.9f, moved.right, .001f)
        assertEquals(.7f, moved.bottom, .001f)
    }

    @Test fun touchingOutsideFrameDoesNothing() {
        val gesture = OcrCropGeometry.hitTest(OcrPoint(70f, 250f), viewport, rect, 32f, 20f)
        assertEquals(OcrCropGesture.NONE, gesture)
        assertEquals(rect, OcrCropGeometry.resize(rect, gesture, .5f, .5f))
    }

    @Test fun resizingNeverCrossesMinimumSize() {
        val resized = OcrCropGeometry.resize(rect, OcrCropGesture.RIGHT, -1f, 0f)
        assertEquals(rect.left + OcrCropGeometry.MIN_SIZE, resized.right, .001f)
        assertTrue(resized.left < resized.right)
    }

    @Test fun resizingStaysInsideImageBounds() {
        val resized = OcrCropGeometry.resize(rect, OcrCropGesture.BOTTOM_RIGHT, 2f, 2f)
        assertEquals(1f, resized.right, .001f)
        assertEquals(1f, resized.bottom, .001f)
    }

    @Test fun fitViewportAccountsForVerticalMargins() {
        val fitted = OcrCropGeometry.imageViewport(1000f, 1000f, 1000, 500)
        assertEquals(OcrImageViewport(0f, 250f, 1000f, 750f), fitted)
    }

    @Test fun initialFullFrameKeepsCornerHitboxesInsideCanvas() {
        val fitted = OcrCropGeometry.imageViewport(1000f, 500f, 1000, 500, insetPx = 32f)
        assertTrue(fitted.left >= 32f)
        assertTrue(fitted.top >= 32f)
        assertTrue(fitted.right <= 968f)
        assertTrue(fitted.bottom <= 468f)
    }
}

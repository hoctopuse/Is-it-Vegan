package com.example.isitvegan

import kotlin.math.abs
import kotlin.math.min

/** A crop rectangle expressed as fractions of the displayed, already oriented bitmap. */
internal data class OcrCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    companion object {
        fun full() = OcrCropRect(0f, 0f, 1f, 1f)
    }

    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

internal data class OcrPoint(val x: Float, val y: Float)

internal data class OcrImageViewport(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun contains(point: OcrPoint): Boolean =
        point.x in left..right && point.y in top..bottom
}

internal enum class OcrCropGesture {
    NONE,
    MOVE,
    LEFT,
    TOP,
    RIGHT,
    BOTTOM,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    val resizes: Boolean get() = this != NONE && this != MOVE
}

internal object OcrCropGeometry {
    const val MIN_SIZE = 0.12f

    fun clamp(rect: OcrCropRect, minSize: Float = MIN_SIZE): OcrCropRect {
        val size = minSize.coerceIn(0.01f, 1f)
        val left = rect.left.coerceIn(0f, 1f - size)
        val top = rect.top.coerceIn(0f, 1f - size)
        val right = rect.right.coerceIn(left + size, 1f)
        val bottom = rect.bottom.coerceIn(top + size, 1f)
        return OcrCropRect(left, top, right, bottom)
    }

    fun move(rect: OcrCropRect, deltaX: Float, deltaY: Float): OcrCropRect {
        val safe = clamp(rect)
        val left = (safe.left + deltaX).coerceIn(0f, 1f - safe.width)
        val top = (safe.top + deltaY).coerceIn(0f, 1f - safe.height)
        return OcrCropRect(left, top, left + safe.width, top + safe.height)
    }

    fun resize(rect: OcrCropRect, gesture: OcrCropGesture, deltaX: Float, deltaY: Float): OcrCropRect {
        val safe = clamp(rect)
        if (!gesture.resizes) return if (gesture == OcrCropGesture.MOVE) move(safe, deltaX, deltaY) else safe

        val left = if (gesture in leftGestures) {
            (safe.left + deltaX).coerceIn(0f, safe.right - MIN_SIZE)
        } else safe.left
        val right = if (gesture in rightGestures) {
            (safe.right + deltaX).coerceIn(safe.left + MIN_SIZE, 1f)
        } else safe.right
        val top = if (gesture in topGestures) {
            (safe.top + deltaY).coerceIn(0f, safe.bottom - MIN_SIZE)
        } else safe.top
        val bottom = if (gesture in bottomGestures) {
            (safe.bottom + deltaY).coerceIn(safe.top + MIN_SIZE, 1f)
        } else safe.bottom
        return OcrCropRect(left, top, right, bottom)
    }

    fun resizeBottomRight(rect: OcrCropRect, deltaX: Float, deltaY: Float): OcrCropRect =
        resize(rect, OcrCropGesture.BOTTOM_RIGHT, deltaX, deltaY)

    fun resizeTopLeft(rect: OcrCropRect, deltaX: Float, deltaY: Float): OcrCropRect {
        return resize(rect, OcrCropGesture.TOP_LEFT, deltaX, deltaY)
    }

    fun imageViewport(
        containerWidth: Float,
        containerHeight: Float,
        imageWidth: Int,
        imageHeight: Int,
        insetPx: Float = 0f
    ): OcrImageViewport {
        if (containerWidth <= 0f || containerHeight <= 0f || imageWidth <= 0 || imageHeight <= 0) {
            return OcrImageViewport(0f, 0f, 0f, 0f)
        }
        val inset = insetPx.coerceAtLeast(0f)
        val availableWidth = (containerWidth - inset * 2f).coerceAtLeast(1f)
        val availableHeight = (containerHeight - inset * 2f).coerceAtLeast(1f)
        val scale = min(availableWidth / imageWidth, availableHeight / imageHeight)
        val width = imageWidth * scale
        val height = imageHeight * scale
        val left = (containerWidth - width) / 2f
        val top = (containerHeight - height) / 2f
        return OcrImageViewport(left, top, left + width, top + height)
    }

    fun hitTest(
        point: OcrPoint,
        viewport: OcrImageViewport,
        rect: OcrCropRect,
        cornerRadiusPx: Float,
        edgeRadiusPx: Float
    ): OcrCropGesture {
        if (viewport.width <= 0f || viewport.height <= 0f) return OcrCropGesture.NONE
        val safe = clamp(rect)
        val left = viewport.left + safe.left * viewport.width
        val top = viewport.top + safe.top * viewport.height
        val right = viewport.left + safe.right * viewport.width
        val bottom = viewport.top + safe.bottom * viewport.height

        listOf(
            OcrCropGesture.TOP_LEFT to OcrPoint(left, top),
            OcrCropGesture.TOP_RIGHT to OcrPoint(right, top),
            OcrCropGesture.BOTTOM_LEFT to OcrPoint(left, bottom),
            OcrCropGesture.BOTTOM_RIGHT to OcrPoint(right, bottom)
        ).firstOrNull { (_, corner) ->
            abs(point.x - corner.x) <= cornerRadiusPx && abs(point.y - corner.y) <= cornerRadiusPx
        }?.let { return it.first }

        if (point.y in top..bottom) {
            if (abs(point.x - left) <= edgeRadiusPx) return OcrCropGesture.LEFT
            if (abs(point.x - right) <= edgeRadiusPx) return OcrCropGesture.RIGHT
        }
        if (point.x in left..right) {
            if (abs(point.y - top) <= edgeRadiusPx) return OcrCropGesture.TOP
            if (abs(point.y - bottom) <= edgeRadiusPx) return OcrCropGesture.BOTTOM
        }
        return if (point.x in left..right && point.y in top..bottom) OcrCropGesture.MOVE else OcrCropGesture.NONE
    }

    private val leftGestures = setOf(OcrCropGesture.LEFT, OcrCropGesture.TOP_LEFT, OcrCropGesture.BOTTOM_LEFT)
    private val rightGestures = setOf(OcrCropGesture.RIGHT, OcrCropGesture.TOP_RIGHT, OcrCropGesture.BOTTOM_RIGHT)
    private val topGestures = setOf(OcrCropGesture.TOP, OcrCropGesture.TOP_LEFT, OcrCropGesture.TOP_RIGHT)
    private val bottomGestures = setOf(OcrCropGesture.BOTTOM, OcrCropGesture.BOTTOM_LEFT, OcrCropGesture.BOTTOM_RIGHT)
}

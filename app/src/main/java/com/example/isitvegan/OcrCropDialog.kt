package com.example.isitvegan

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun OcrCropDialog(
    bitmap: Bitmap,
    initialRect: OcrCropRect = OcrCropRect.full(),
    onCancel: () -> Unit,
    onConfirm: (OcrCropRect) -> Unit
) {
    var cropRect by remember(bitmap, initialRect) { mutableStateOf(OcrCropGeometry.clamp(initialRect)) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Recadrer la zone à analyser", style = MaterialTheme.typography.titleLarge)
                Text("Déplacez le cadre ou utilisez ses poignées pour isoler la zone utile.")
                CropImageEditor(bitmap, cropRect) { cropRect = it }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("ANNULER") }
                    Button(onClick = { onConfirm(OcrCropGeometry.clamp(cropRect)) }, modifier = Modifier.weight(1f)) {
                        Text("UTILISER CE CADRE")
                    }
                }
            }
        }
    }
}

@Composable
private fun CropImageEditor(bitmap: Bitmap, rect: OcrCropRect, onRectChange: (OcrCropRect) -> Unit) {
    val aspect = (bitmap.width.toFloat() / bitmap.height.toFloat()).coerceIn(0.65f, 1.8f)
    val latestRect by rememberUpdatedState(rect)
    var activeGesture by remember { mutableStateOf(OcrCropGesture.NONE) }
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { 32.dp.toPx() }
    val edgeRadiusPx = with(density) { 20.dp.toPx() }
    val handleRadiusPx = with(density) { 8.dp.toPx() }
    val activeColor = MaterialTheme.colorScheme.primary
    Box(Modifier.fillMaxWidth().heightIn(max = 520.dp).aspectRatio(aspect)) {
        Image(
            bitmap.asImageBitmap(),
            "Image à recadrer",
            Modifier.fillMaxSize().padding(32.dp),
            contentScale = ContentScale.Fit
        )
        Canvas(
            Modifier.fillMaxSize().pointerInput(bitmap, cornerRadiusPx, edgeRadiusPx) {
                var gesture = OcrCropGesture.NONE
                detectDragGestures(
                    onDragStart = { position ->
                        val viewport = OcrCropGeometry.imageViewport(
                            size.width.toFloat(), size.height.toFloat(), bitmap.width, bitmap.height, cornerRadiusPx
                        )
                        gesture = OcrCropGeometry.hitTest(
                            OcrPoint(position.x, position.y), viewport, latestRect, cornerRadiusPx, edgeRadiusPx
                        )
                        activeGesture = gesture
                    },
                    onDrag = { change, dragAmount ->
                        if (gesture == OcrCropGesture.NONE) return@detectDragGestures
                        change.consume()
                        val viewport = OcrCropGeometry.imageViewport(
                            size.width.toFloat(), size.height.toFloat(), bitmap.width, bitmap.height, cornerRadiusPx
                        )
                        val dx = dragAmount.x / viewport.width
                        val dy = dragAmount.y / viewport.height
                        val updated = OcrCropGeometry.resize(latestRect, gesture, dx, dy)
                        onRectChange(updated)
                    },
                    onDragEnd = { activeGesture = OcrCropGesture.NONE },
                    onDragCancel = { activeGesture = OcrCropGesture.NONE }
                )
            }
        ) {
            val viewport = OcrCropGeometry.imageViewport(
                size.width, size.height, bitmap.width, bitmap.height, cornerRadiusPx
            )
            val l = viewport.left + rect.left * viewport.width
            val t = viewport.top + rect.top * viewport.height
            val r = viewport.left + rect.right * viewport.width
            val b = viewport.top + rect.bottom * viewport.height
            val shade = Color.Black.copy(alpha = 0.48f)
            drawRect(shade, topLeft = Offset(viewport.left, viewport.top), size = androidx.compose.ui.geometry.Size(viewport.width, t - viewport.top))
            drawRect(shade, topLeft = Offset(viewport.left, b), size = androidx.compose.ui.geometry.Size(viewport.width, viewport.bottom - b))
            drawRect(shade, topLeft = Offset(viewport.left, t), size = androidx.compose.ui.geometry.Size(l - viewport.left, b - t))
            drawRect(shade, topLeft = Offset(r, t), size = androidx.compose.ui.geometry.Size(viewport.right - r, b - t))
            drawRect(
                if (activeGesture == OcrCropGesture.MOVE) activeColor else Color.White,
                topLeft = Offset(l, t),
                size = androidx.compose.ui.geometry.Size(r - l, b - t),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )

            val corners = listOf(
                OcrCropGesture.TOP_LEFT to Offset(l, t),
                OcrCropGesture.TOP_RIGHT to Offset(r, t),
                OcrCropGesture.BOTTOM_LEFT to Offset(l, b),
                OcrCropGesture.BOTTOM_RIGHT to Offset(r, b)
            )
            corners.forEach { (gesture, center) ->
                drawCircle(Color.Black.copy(alpha = 0.75f), handleRadiusPx + 3f, center)
                drawCircle(if (activeGesture == gesture) activeColor else Color.White, handleRadiusPx, center)
            }

            val edgeColor = activeColor
            val edgeWidth = 7f
            when (activeGesture) {
                OcrCropGesture.LEFT -> drawLine(edgeColor, Offset(l, t), Offset(l, b), edgeWidth, StrokeCap.Round)
                OcrCropGesture.TOP -> drawLine(edgeColor, Offset(l, t), Offset(r, t), edgeWidth, StrokeCap.Round)
                OcrCropGesture.RIGHT -> drawLine(edgeColor, Offset(r, t), Offset(r, b), edgeWidth, StrokeCap.Round)
                OcrCropGesture.BOTTOM -> drawLine(edgeColor, Offset(l, b), Offset(r, b), edgeWidth, StrokeCap.Round)
                else -> Unit
            }
        }
    }
}

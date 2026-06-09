package com.mauricior8.enfoque.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/**
 * Mini editor to fit a gallery image into a square app icon. The user pans by
 * dragging and zooms with the slider; the square viewport is exactly what gets
 * saved (256px). Reports the transform so the caller can render the same crop.
 */
@Composable
fun IconEditorDialog(
    imageUri: Uri,
    onConfirm: (boxPx: Float, scale: Float, txPx: Float, tyPx: Float) -> Unit,
    onCancel: () -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    val viewportDp = 260.dp
    val boxPx = with(LocalDensity.current) { viewportDp.toPx() }

    var scale by remember { mutableFloatStateOf(1f) }
    var tx by remember { mutableFloatStateOf(0f) }
    var ty by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onCancel) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.background)
                .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ajustar icono", color = colors.content, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Arrastra para centrar y usa la barra para acercar.", color = colors.secondaryContent)
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(viewportDp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.subtleFill)
                        .border(1.dp, colors.stroke, RoundedCornerShape(24.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                tx += dragAmount.x
                                ty += dragAmount.y
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Imagen del icono",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(viewportDp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = tx,
                                translationY = ty,
                            ),
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Zoom", color = colors.secondaryContent)
                    Spacer(Modifier.width(12.dp))
                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 1f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.content,
                            activeTrackColor = colors.content,
                            inactiveTrackColor = colors.subtleFill,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Centrar",
                        color = colors.secondaryContent,
                        modifier = Modifier.clickable { scale = 1f; tx = 0f; ty = 0f }.padding(12.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Cancelar", color = colors.secondaryContent, modifier = Modifier.clickable { onCancel() }.padding(12.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Guardar",
                        color = colors.content,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.subtleFill)
                            .clickable { onConfirm(boxPx, scale, tx, ty) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

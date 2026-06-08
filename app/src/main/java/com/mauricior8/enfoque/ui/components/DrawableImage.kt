package com.mauricior8.enfoque.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

/**
 * Renders an Android [Drawable] (such as an app icon) inside Compose by
 * converting it to an ImageBitmap. Returns an empty box when [drawable] is null.
 */
@Composable
fun DrawableImage(
    drawable: Drawable?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (drawable == null) {
        Box(modifier)
        return
    }
    val bitmap = remember(drawable) {
        runCatching {
            drawable.toBitmap(
                width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 144,
                height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 144,
            ).asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(modifier.size(48.dp))
    }
}

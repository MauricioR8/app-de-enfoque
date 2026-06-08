package com.mauricior8.enfoque.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/**
 * A single home/folder grid cell: an icon with a small label underneath.
 * Supports tap and long-press (for editing / removing).
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun IconCell(
    label: String,
    icon: Drawable?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    iconSize: Int = 56,
    overlay: (@Composable () -> Unit)? = null,
) {
    val colors = LocalEnfoqueColors.current
    Column(
        modifier = modifier
            .width(80.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(contentAlignment = Alignment.Center) {
            DrawableImage(
                drawable = icon,
                contentDescription = label,
                modifier = Modifier.size(iconSize.dp),
            )
            overlay?.invoke()
        }
        Text(
            text = label,
            color = colors.content,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

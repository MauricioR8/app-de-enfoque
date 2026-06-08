package com.mauricior8.enfoque.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.ui.components.DrawableImage
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import java.io.File

/**
 * A folder shown on the home screen. If the user set a custom image it is used
 * (cropped to a square); otherwise a 2x2 preview of the contained app icons is
 * drawn inside a rounded, bordered tile.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCell(
    folder: Folder,
    items: List<LaunchableItem>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 56,
) {
    val colors = LocalEnfoqueColors.current
    val contained = items.filter { folder.itemKeys.contains(it.key) }

    Column(
        modifier = modifier
            .width(80.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(shape)
                .border(1.dp, colors.stroke, shape),
            contentAlignment = Alignment.Center,
        ) {
            val customPath = folder.customImagePath
            if (!customPath.isNullOrBlank() && File(customPath).exists()) {
                AsyncImage(
                    model = File(customPath),
                    contentDescription = folder.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(shape),
                )
            } else {
                // 2x2 mini preview of contained icons.
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    contained.take(4).chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            rowItems.forEach { item ->
                                DrawableImage(
                                    drawable = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = folder.name,
            color = colors.content,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

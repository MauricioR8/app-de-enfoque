package com.mauricior8.enfoque.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.HomeEntry
import com.mauricior8.enfoque.data.model.HomeLayout
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.ui.components.IconCell
import com.mauricior8.enfoque.ui.components.tappable
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/**
 * The launcher home screen: a clock framed by an arc, the launcher name, the
 * configured app/folder grid, and phone + camera shortcuts at the bottom.
 */
@Composable
fun HomeScreen(
    launcherName: String,
    layout: HomeLayout,
    items: List<LaunchableItem>,
    onItemClick: (LaunchableItem) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onItemLongClick: (HomeEntry) -> Unit,
    onOpenPhone: () -> Unit,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            ClockArc()

            Spacer(Modifier.height(24.dp))

            // App / folder grid
            if (layout.entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Mantén pulsada una app en el cajón para añadirla aquí, o crea una carpeta.",
                        color = colors.secondaryContent,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    itemsIndexed(layout.entries, key = { _, e -> e.id }) { _, entry ->
                        HomeEntryCell(
                            entry = entry,
                            layout = layout,
                            items = items,
                            onItemClick = onItemClick,
                            onFolderClick = onFolderClick,
                            onLongClick = { onItemLongClick(entry) },
                        )
                    }
                }
            }

            // Launcher name
            Text(
                text = launcherName,
                color = colors.content,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Bottom-left phone, bottom-right camera
        Icon(
            imageVector = Icons.Outlined.Phone,
            contentDescription = "Teléfono",
            tint = colors.content,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 28.dp)
                .tappable(onOpenPhone),
        )
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = "Cámara",
            tint = colors.content,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 28.dp)
                .tappable(onOpenCamera),
        )
    }
}

@Composable
private fun HomeEntryCell(
    entry: HomeEntry,
    layout: HomeLayout,
    items: List<LaunchableItem>,
    onItemClick: (LaunchableItem) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onLongClick: () -> Unit,
) {
    when (entry) {
        is HomeEntry.AppRef -> {
            val item = items.firstOrNull {
                it.type == LaunchableItem.Type.APP &&
                    it.packageName == entry.packageName &&
                    it.activityName == entry.activityName
            }
            if (item != null) {
                IconCell(
                    label = item.label,
                    icon = item.icon,
                    onClick = { onItemClick(item) },
                    onLongClick = onLongClick,
                )
            }
        }

        is HomeEntry.WebRef -> {
            val item = items.firstOrNull {
                it.type == LaunchableItem.Type.WEB_SHORTCUT && it.url == entry.url
            }
            IconCell(
                label = entry.label,
                icon = item?.icon,
                onClick = { item?.let(onItemClick) },
                onLongClick = onLongClick,
            )
        }

        is HomeEntry.FolderRef -> {
            val folder = layout.folders.firstOrNull { it.id == entry.id } ?: return
            FolderCell(
                folder = folder,
                items = items,
                onClick = { onFolderClick(folder) },
                onLongClick = onLongClick,
            )
        }
    }
}

package com.mauricior8.enfoque.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * The launcher home screen: a clock framed by an arc, the (optional) launcher
 * name, the configured app/folder grid, and phone + camera shortcuts.
 */
@Composable
fun HomeScreen(
    launcherName: String,
    layout: HomeLayout,
    items: List<LaunchableItem>,
    clock24h: Boolean,
    showSeconds: Boolean,
    arcBattery: Boolean,
    onItemClick: (LaunchableItem) -> Unit,
    onFolderOpen: (Folder) -> Unit,
    onFolderEdit: (Folder) -> Unit,
    onRemoveFromHome: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onAppInfo: (String) -> Unit,
    onClickTime: () -> Unit,
    onClickDate: () -> Unit,
    onClockLongPress: () -> Unit,
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
            ClockArc(
                clock24h = clock24h,
                showSeconds = showSeconds,
                arcBattery = arcBattery,
                onClickTime = onClickTime,
                onClickDate = onClickDate,
                onLongPress = onClockLongPress,
            )

            Spacer(Modifier.height(24.dp))

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
                    items(layout.entries, key = { it.id }) { entry ->
                        HomeEntryCell(
                            entry = entry,
                            layout = layout,
                            items = items,
                            onItemClick = onItemClick,
                            onFolderOpen = onFolderOpen,
                            onFolderEdit = onFolderEdit,
                            onRemoveFromHome = onRemoveFromHome,
                            onUninstall = onUninstall,
                            onAppInfo = onAppInfo,
                        )
                    }
                }
            }

            // Launcher name (hidden when blank).
            if (launcherName.isNotBlank()) {
                Text(
                    text = launcherName,
                    color = colors.content,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeEntryCell(
    entry: HomeEntry,
    layout: HomeLayout,
    items: List<LaunchableItem>,
    onItemClick: (LaunchableItem) -> Unit,
    onFolderOpen: (Folder) -> Unit,
    onFolderEdit: (Folder) -> Unit,
    onRemoveFromHome: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onAppInfo: (String) -> Unit,
) {
    when (entry) {
        is HomeEntry.AppRef -> {
            val item = items.firstOrNull {
                it.type == LaunchableItem.Type.APP &&
                    it.packageName == entry.packageName &&
                    it.activityName == entry.activityName
            }
            if (item != null) {
                AppEntryCell(
                    label = item.label,
                    item = item,
                    packageName = entry.packageName,
                    isApp = true,
                    onClick = { onItemClick(item) },
                    onRemoveFromHome = { onRemoveFromHome(entry.id) },
                    onUninstall = { onUninstall(entry.packageName) },
                    onAppInfo = { onAppInfo(entry.packageName) },
                )
            }
        }

        is HomeEntry.WebRef -> {
            val item = items.firstOrNull {
                it.type == LaunchableItem.Type.WEB_SHORTCUT && it.url == entry.url
            }
            AppEntryCell(
                label = entry.label,
                item = item,
                packageName = null,
                isApp = false,
                onClick = { item?.let(onItemClick) },
                onRemoveFromHome = { onRemoveFromHome(entry.id) },
                onUninstall = {},
                onAppInfo = {},
            )
        }

        is HomeEntry.FolderRef -> {
            val folder = layout.folders.firstOrNull { it.id == entry.id } ?: return
            FolderCell(
                folder = folder,
                items = items,
                onClick = { onFolderOpen(folder) },
                onLongClick = { onFolderEdit(folder) },
            )
        }
    }
}

/** App/web cell on the home screen with a long-press context menu. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppEntryCell(
    label: String,
    item: LaunchableItem?,
    packageName: String?,
    isApp: Boolean,
    onClick: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onUninstall: () -> Unit,
    onAppInfo: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        IconCell(
            label = label,
            icon = item?.icon,
            onClick = onClick,
            onLongClick = { menuOpen = true },
        )
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Quitar de la pantalla") },
                onClick = { menuOpen = false; onRemoveFromHome() },
            )
            if (isApp && packageName != null) {
                DropdownMenuItem(
                    text = { Text("Información de la app") },
                    onClick = { menuOpen = false; onAppInfo() },
                )
                DropdownMenuItem(
                    text = { Text("Desinstalar") },
                    onClick = { menuOpen = false; onUninstall() },
                )
            }
        }
    }
}

package com.mauricior8.enfoque.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.HomeEntry
import com.mauricior8.enfoque.data.model.HomeLayout
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.data.model.ProgressSpan
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
    arcColorByBattery: Boolean,
    onItemClick: (LaunchableItem) -> Unit,
    onFolderOpen: (Folder) -> Unit,
    onFolderEdit: (Folder) -> Unit,
    onRemoveFromHome: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onAppInfo: (String) -> Unit,
    onChangeIcon: (String) -> Unit,
    onResetIcon: (String) -> Unit,
    onClickTime: () -> Unit,
    onClickDate: () -> Unit,
    onClockLongPress: () -> Unit,
    onSetProgress: (ProgressSpan) -> Unit,
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
                arcColorByBattery = arcColorByBattery,
                onClickTime = onClickTime,
                onClickDate = onClickDate,
                onLongPress = onClockLongPress,
            )

            Spacer(Modifier.height(16.dp))

            // Single home progress rectangle (editable / removable).
            if (layout.progressSpan != ProgressSpan.NONE) {
                HomeProgressBar(
                    span = layout.progressSpan,
                    onSetProgress = onSetProgress,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

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
                            onChangeIcon = onChangeIcon,
                            onResetIcon = onResetIcon,
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
    onChangeIcon: (String) -> Unit,
    onResetIcon: (String) -> Unit,
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
                    onChangeIcon = { onChangeIcon(item.key) },
                    onResetIcon = { onResetIcon(item.key) },
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
                onChangeIcon = { item?.let { onChangeIcon(it.key) } },
                onResetIcon = { item?.let { onResetIcon(it.key) } },
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
    onChangeIcon: () -> Unit,
    onResetIcon: () -> Unit,
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
            DropdownMenuItem(
                text = { Text("Cambiar imagen del icono") },
                onClick = { menuOpen = false; onChangeIcon() },
            )
            DropdownMenuItem(
                text = { Text("Restaurar icono original") },
                onClick = { menuOpen = false; onResetIcon() },
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



/** Single home-screen progress rectangle. Long-press to change span or remove. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeProgressBar(
    span: ProgressSpan,
    onSetProgress: (ProgressSpan) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    var menuOpen by remember { mutableStateOf(false) }
    val fraction = remember(span, System.currentTimeMillis() / 60000) { progressFraction(span) }
    val label = when (span) {
        ProgressSpan.DAY -> "Progreso del Día"
        ProgressSpan.WEEK -> "Progreso de la Semana"
        ProgressSpan.MONTH -> "Progreso del Mes"
        ProgressSpan.YEAR -> "Progreso del Año"
        ProgressSpan.NONE -> ""
    }

    Box {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { menuOpen = true }, onLongClick = { menuOpen = true }),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(label, color = colors.content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("${(fraction * 100).toInt()}%", color = colors.secondaryContent, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.subtleFill),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.content),
                )
            }
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(text = { Text("Día") }, onClick = { menuOpen = false; onSetProgress(ProgressSpan.DAY) })
            DropdownMenuItem(text = { Text("Semana") }, onClick = { menuOpen = false; onSetProgress(ProgressSpan.WEEK) })
            DropdownMenuItem(text = { Text("Mes") }, onClick = { menuOpen = false; onSetProgress(ProgressSpan.MONTH) })
            DropdownMenuItem(text = { Text("Año") }, onClick = { menuOpen = false; onSetProgress(ProgressSpan.YEAR) })
            DropdownMenuItem(text = { Text("Quitar") }, onClick = { menuOpen = false; onSetProgress(ProgressSpan.NONE) })
        }
    }
}

private fun progressFraction(span: ProgressSpan): Float {
    val date = java.time.LocalDate.now()
    val time = java.time.LocalTime.now()
    val dayFrac = time.toSecondOfDay().toFloat() / 86400f
    return when (span) {
        ProgressSpan.DAY -> dayFrac
        ProgressSpan.WEEK -> ((date.dayOfWeek.value - 1) + dayFrac) / 7f
        ProgressSpan.MONTH -> {
            val len = java.time.YearMonth.from(date).lengthOfMonth()
            ((date.dayOfMonth - 1) + dayFrac) / len
        }
        ProgressSpan.YEAR -> {
            val len = if (date.isLeapYear) 366 else 365
            ((date.dayOfYear - 1) + dayFrac) / len
        }
        ProgressSpan.NONE -> 0f
    }
}

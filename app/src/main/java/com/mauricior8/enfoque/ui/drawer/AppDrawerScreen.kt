package com.mauricior8.enfoque.ui.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.ui.components.DrawableImage
import com.mauricior8.enfoque.ui.components.tappable
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import kotlinx.coroutines.launch

/** Action chosen from an app's long-press menu. */
enum class DrawerItemAction { ADD_TO_HOME, ADD_TO_FOLDER, CHANGE_ICON, RESET_ICON, APP_INFO, UNINSTALL, REMOVE_SHORTCUT }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    items: List<LaunchableItem>,
    onItemClick: (LaunchableItem) -> Unit,
    onItemAction: (LaunchableItem, DrawerItemAction) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    var query by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val filtered = remember(items, query) {
        if (query.isBlank()) items
        else items.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }

    val recent = remember(items) {
        items.sortedByDescending { it.firstInstallTime }.take(5)
    }

    // Build a flat list of rows: optional "recent" header + recents, then A-Z.
    val rows = remember(filtered, recent, query) { buildRows(filtered, recent, query.isBlank()) }

    // Map of section letter -> first row index, for the alphabet index.
    val letterIndex = remember(rows) {
        buildMap {
            rows.forEachIndexed { index, row ->
                if (row is DrawerRow.Header && row.isLetter) {
                    putIfAbsent(row.title, index)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Search bar with magnifier on the right.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.content, fontSize = 18.sp),
                    cursorBrush = SolidColor(colors.content),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            filtered.firstOrNull()?.let(onItemClick)
                        }
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                "Buscar",
                                color = colors.secondaryContent,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    tint = colors.content,
                    modifier = Modifier.size(24.dp),
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 24.dp), // leave room for the alphabet index
                ) {
                    itemsIndexed(rows) { _, row ->
                        when (row) {
                            is DrawerRow.Header -> SectionHeader(row.title)
                            is DrawerRow.Item -> AppRow(
                                item = row.item,
                                onClick = { onItemClick(row.item) },
                                onAction = { action -> onItemAction(row.item, action) },
                            )
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }

                // Vertical A-Z index on the right edge.
                if (query.isBlank()) {
                    AlphabetIndex(
                        letters = letterIndex.keys.toList(),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(22.dp),
                        onLetterSelected = { letter ->
                            letterIndex[letter]?.let { idx ->
                                scope.launch { listState.scrollToItem(idx) }
                            }
                        },
                    )
                }
            }
        }

        // Settings gear (bottom-right).
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Ajustes",
            tint = colors.content,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(26.dp)
                .tappable { onOpenSettings() },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = LocalEnfoqueColors.current
    Text(
        text = title,
        color = colors.secondaryContent,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 6.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    item: LaunchableItem,
    onClick: () -> Unit,
    onAction: (DrawerItemAction) -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    var menuOpen by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuOpen = true },
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DrawableImage(
                drawable = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(34.dp),
            )
            Spacer(Modifier.width(18.dp))
            Text(
                text = item.label,
                color = colors.content,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Añadir a la pantalla principal") },
                onClick = { menuOpen = false; onAction(DrawerItemAction.ADD_TO_HOME) },
            )
            DropdownMenuItem(
                text = { Text("Añadir a una carpeta") },
                onClick = { menuOpen = false; onAction(DrawerItemAction.ADD_TO_FOLDER) },
            )
            DropdownMenuItem(
                text = { Text("Cambiar imagen del icono") },
                onClick = { menuOpen = false; onAction(DrawerItemAction.CHANGE_ICON) },
            )
            DropdownMenuItem(
                text = { Text("Restaurar icono original") },
                onClick = { menuOpen = false; onAction(DrawerItemAction.RESET_ICON) },
            )
            if (item.type == LaunchableItem.Type.APP) {
                DropdownMenuItem(
                    text = { Text("Información de la app") },
                    onClick = { menuOpen = false; onAction(DrawerItemAction.APP_INFO) },
                )
                DropdownMenuItem(
                    text = { Text("Desinstalar") },
                    onClick = { menuOpen = false; onAction(DrawerItemAction.UNINSTALL) },
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Eliminar acceso directo") },
                    onClick = { menuOpen = false; onAction(DrawerItemAction.REMOVE_SHORTCUT) },
                )
            }
        }
    }
}

@Composable
private fun AlphabetIndex(
    letters: List<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    if (letters.isEmpty()) return

    Column(
        modifier = modifier.pointerInput(letters) {
            detectVerticalDragGestures { change, _ ->
                val y = change.position.y.coerceIn(0f, size.height.toFloat())
                val fraction = y / size.height
                val idx = (fraction * letters.size).toInt().coerceIn(0, letters.size - 1)
                onLetterSelected(letters[idx])
            }
        },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        letters.forEach { letter ->
            Text(
                text = letter,
                color = colors.secondaryContent,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .tappable { onLetterSelected(letter) },
            )
        }
    }
}

/* ----------------------------- Row model ----------------------------- */

private sealed class DrawerRow {
    data class Header(val title: String, val isLetter: Boolean) : DrawerRow()
    data class Item(val item: LaunchableItem) : DrawerRow()
}

private fun buildRows(
    items: List<LaunchableItem>,
    recent: List<LaunchableItem>,
    includeRecent: Boolean,
): List<DrawerRow> {
    val rows = mutableListOf<DrawerRow>()
    if (includeRecent && recent.isNotEmpty()) {
        rows += DrawerRow.Header("Instaladas recientemente", isLetter = false)
        recent.forEach { rows += DrawerRow.Item(it) }
    }
    var currentLetter: String? = null
    items.forEach { item ->
        val letter = item.sectionLetter
        if (letter != currentLetter) {
            currentLetter = letter
            rows += DrawerRow.Header(letter, isLetter = true)
        }
        rows += DrawerRow.Item(item)
    }
    return rows
}

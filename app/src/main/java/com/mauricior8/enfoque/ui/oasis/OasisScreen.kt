package com.mauricior8.enfoque.ui.oasis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mauricior8.enfoque.data.model.OasisBoard
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.data.model.WidgetType
import com.mauricior8.enfoque.ui.components.OasisCard
import com.mauricior8.enfoque.ui.components.PillButton
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/**
 * The Oasis productivity screen. Shows the "Widgets | Oasis" tabs, the list of
 * configurable widget cards, and a pill button to add new widgets. All features
 * are unlocked (no "Obtener Pro" buttons).
 */
@Composable
fun OasisScreen(
    board: OasisBoard,
    onWidgetChange: (WidgetConfig) -> Unit,
    onWidgetRemove: (String) -> Unit,
    onAddWidget: (WidgetType) -> Unit,
    onExpandNote: (WidgetConfig, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    var selectedTab by remember { mutableStateOf(1) } // 0 = Widgets, 1 = Oasis
    var showAddDialog by remember { mutableStateOf(false) }

    // Favorites first, then the rest in their saved order.
    val orderedWidgets = remember(board.widgets) {
        board.widgets.sortedByDescending { it.favorite }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabLabel("Widgets", selectedTab == 0) { selectedTab = 0 }
            Text(" | ", color = colors.secondaryContent)
            TabLabel("Oasis", selectedTab == 1) { selectedTab = 1 }
        }

        if (selectedTab == 0) {
            WidgetsCatalogTab(onAddWidget = onAddWidget)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(orderedWidgets, key = { it.id }) { widget ->
                    WidgetDispatcher(
                        config = widget,
                        onChange = onWidgetChange,
                        onRemove = { onWidgetRemove(widget.id) },
                        onExpandNote = onExpandNote,
                    )
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        PillButton(text = "+ Añadir widget", onClick = { showAddDialog = true })
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddWidgetDialog(
            onDismiss = { showAddDialog = false },
            onPick = { type ->
                showAddDialog = false
                onAddWidget(type)
            },
        )
    }
}

@Composable
private fun TabLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalEnfoqueColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(horizontal = 12.dp)) {
        Text(
            text = text,
            color = if (selected) colors.content else colors.secondaryContent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(if (selected) 48.dp else 0.dp)
                .background(colors.content),
        )
    }
}

@Composable
private fun WidgetDispatcher(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    onExpandNote: (WidgetConfig, Int) -> Unit,
) {
    when (config.type) {
        WidgetType.TODO -> TodoWidget(config, onChange, onRemove)
        WidgetType.NOTES -> NotesWidget(config, onChange, onRemove, onExpandNote)
        WidgetType.CALENDAR -> CalendarWidget(config, onChange, onRemove)
        WidgetType.POMODORO -> PomodoroWidget(config, onChange, onRemove)
        WidgetType.TIME_PROGRESS -> TimeProgressWidget(config, onChange, onRemove)
        WidgetType.MINI_GAME -> MiniGameWidget(config, onChange, onRemove)
        WidgetType.CUSTOM -> CustomWidget(config, onChange, onRemove)
    }
}

@Composable
private fun WidgetsCatalogTab(onAddWidget: (WidgetType) -> Unit) {
    val colors = LocalEnfoqueColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Añade módulos a tu pantalla Oasis. Todas las funciones están desbloqueadas.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
        )
        widgetCatalog().forEach { (type, label, description) ->
            OasisCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddWidget(type) }
            ) {
                Column {
                    Text(label, color = colors.content, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(description, color = colors.secondaryContent, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun AddWidgetDialog(onDismiss: () -> Unit, onPick: (WidgetType) -> Unit) {
    val colors = LocalEnfoqueColors.current
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(colors.background)
                .padding(4.dp),
        ) {
            OasisCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Añadir widget", color = colors.content, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    widgetCatalog().forEach { (type, label, _) ->
                        Text(
                            text = label,
                            color = colors.content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(type) }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun widgetCatalog(): List<Triple<WidgetType, String, String>> = listOf(
    Triple(WidgetType.TODO, "Por hacer", "Lista de tareas con casillas de verificación."),
    Triple(WidgetType.NOTES, "Notas", "Varias notas rápidas con paginación."),
    Triple(WidgetType.CALENDAR, "Calendario", "Eventos del día con hora y título."),
    Triple(WidgetType.POMODORO, "Temporizador Pomodoro", "Enfoque, pausa corta y pausa larga."),
    Triple(WidgetType.TIME_PROGRESS, "Progreso del Tiempo", "Año, mes, semana y día."),
    Triple(WidgetType.MINI_GAME, "Mini-juego", "Agilidad mental: 2048 y más."),
    Triple(WidgetType.CUSTOM, "Módulo personalizado", "Una tarjeta 100% personalizable."),
)

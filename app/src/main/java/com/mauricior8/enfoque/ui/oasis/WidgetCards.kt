package com.mauricior8.enfoque.ui.oasis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauricior8.enfoque.data.model.CalendarEvent
import com.mauricior8.enfoque.data.model.TodoItem
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.ui.components.OasisCard
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/* ============================ Shared header ============================ */

@Composable
private fun WidgetHeader(
    title: String,
    favorite: Boolean,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalEnfoqueColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = colors.content,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
        Icon(
            imageVector = if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = "Favorito",
            tint = colors.content,
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleFavorite() },
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "Quitar widget",
            tint = colors.secondaryContent,
            modifier = Modifier
                .size(20.dp)
                .clickable { onRemove() },
        )
    }
}

/* ============================ To-Do widget ============================ */

@Composable
fun TodoWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    OasisCard(modifier = modifier) {
        Column {
            WidgetHeader(
                title = config.title.ifBlank { "Por hacer" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
            )
            Spacer(Modifier.height(12.dp))

            config.todos.forEach { todo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Checkbox
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(1.dp, colors.stroke, RoundedCornerShape(4.dp))
                            .background(
                                if (todo.done) colors.subtleFill else androidx.compose.ui.graphics.Color.Transparent,
                                RoundedCornerShape(4.dp),
                            )
                            .clickable {
                                onChange(config.copy(todos = config.todos.map {
                                    if (it.id == todo.id) it.copy(done = !it.done) else it
                                }))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (todo.done) {
                            Text("✓", color = colors.content, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = todo.text,
                        onValueChange = { newText ->
                            onChange(config.copy(todos = config.todos.map {
                                if (it.id == todo.id) it.copy(text = newText) else it
                            }))
                        },
                        textStyle = TextStyle(
                            color = if (todo.done) colors.secondaryContent else colors.content,
                            fontSize = 16.sp,
                            textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                        ),
                        cursorBrush = SolidColor(colors.content),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (todo.text.isEmpty()) {
                                Text("Nueva tarea", color = colors.secondaryContent, fontSize = 16.sp)
                            }
                            inner()
                        },
                    )
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Eliminar",
                        tint = colors.secondaryContent,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onChange(config.copy(todos = config.todos.filterNot { it.id == todo.id }))
                            },
                    )
                }
            }

            // Add button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Añadir tarea",
                    tint = colors.content,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onChange(config.copy(todos = config.todos + TodoItem())) },
                )
            }
        }
    }
}

/* ============================ Notes widget ============================ */

@Composable
fun NotesWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    onExpand: (WidgetConfig, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    var page by remember { mutableIntStateOf(0) }
    val notes = config.notes.ifEmpty { listOf(com.mauricior8.enfoque.data.model.NotePage()) }
    val safePage = page.coerceIn(0, notes.size - 1)

    OasisCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = config.title.ifBlank { "Notas" },
                    color = colors.content,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Outlined.KeyboardArrowLeft, "Anterior", tint = colors.content,
                    modifier = Modifier.size(24.dp).clickable { if (safePage > 0) page = safePage - 1 },
                )
                Text(
                    "${safePage + 1}/${notes.size}",
                    color = colors.content,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
                Icon(
                    Icons.Outlined.KeyboardArrowRight, "Siguiente", tint = colors.content,
                    modifier = Modifier.size(24.dp).clickable {
                        if (safePage < notes.size - 1) page = safePage + 1
                        else { onChange(config.copy(notes = notes + com.mauricior8.enfoque.data.model.NotePage())); page = notes.size }
                    },
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Fullscreen, "Expandir", tint = colors.content,
                    modifier = Modifier.size(22.dp).clickable { onExpand(config, safePage) },
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.Close, "Quitar", tint = colors.secondaryContent,
                    modifier = Modifier.size(20.dp).clickable { onRemove() },
                )
            }
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = notes[safePage].text,
                onValueChange = { newText ->
                    val updated = notes.toMutableList()
                    updated[safePage] = updated[safePage].copy(text = newText)
                    onChange(config.copy(notes = updated))
                },
                textStyle = TextStyle(color = colors.content, fontSize = 16.sp),
                cursorBrush = SolidColor(colors.content),
                modifier = Modifier.fillMaxWidth().height(80.dp),
                decorationBox = { inner ->
                    if (notes[safePage].text.isEmpty()) {
                        Text(
                            "Anota rápidamente tus pensamientos o cosas que no quieres olvidar",
                            color = colors.secondaryContent, fontSize = 16.sp,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

/* ============================ Calendar widget ============================ */

@Composable
fun CalendarWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    var date by remember { mutableStateOf(LocalDate.now()) }
    val epochDay = date.toEpochDay()
    val dayEvents = config.events.filter { it.epochDay == epochDay }.sortedBy { it.startMinutes }

    OasisCard(modifier = modifier) {
        Column {
            WidgetHeader(
                title = config.title.ifBlank { "Calendario" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Outlined.KeyboardArrowLeft, "Día anterior", tint = colors.content,
                    modifier = Modifier.size(24.dp).clickable { date = date.minusDays(1) },
                )
                Text(
                    text = formatLongDate(date),
                    color = colors.content,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Icon(
                    Icons.Outlined.KeyboardArrowRight, "Día siguiente", tint = colors.content,
                    modifier = Modifier.size(24.dp).clickable { date = date.plusDays(1) },
                )
            }
            Spacer(Modifier.height(12.dp))
            if (dayEvents.isEmpty()) {
                Text("Sin eventos", color = colors.secondaryContent)
            } else {
                dayEvents.forEach { ev ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ev.title, color = colors.content, style = MaterialTheme.typography.bodyLarge)
                            if (ev.startMinutes >= 0) {
                                Text(
                                    timeRange(ev),
                                    color = colors.secondaryContent,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        Icon(
                            Icons.Outlined.Close, "Eliminar", tint = colors.secondaryContent,
                            modifier = Modifier.size(18.dp).clickable {
                                onChange(config.copy(events = config.events.filterNot { it.id == ev.id }))
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Add, "Añadir evento", tint = colors.content,
                    modifier = Modifier.size(26.dp).clickable {
                        onChange(
                            config.copy(
                                events = config.events + CalendarEvent(
                                    epochDay = epochDay,
                                    title = "Nuevo evento",
                                )
                            )
                        )
                    },
                )
            }
        }
    }
}

/* ============================ Pomodoro widget ============================ */

@Composable
fun PomodoroWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    // mode: 0 = focus, 1 = short break, 2 = long break
    var mode by remember { mutableIntStateOf(0) }
    val totalSeconds = when (mode) {
        1 -> config.pomodoroShortBreak * 60
        2 -> config.pomodoroLongBreak * 60
        else -> config.pomodoroFocus * 60
    }
    var remaining by remember(mode, totalSeconds) { mutableIntStateOf(totalSeconds) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, mode) {
        while (running && remaining > 0) {
            kotlinx.coroutines.delay(1000L)
            remaining -= 1
        }
        if (remaining <= 0) running = false
    }

    OasisCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WidgetHeader(
                title = config.title.ifBlank { "Temporizador Pomodoro" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PomodoroTab("Pomodoro", mode == 0) { mode = 0; running = false }
                PomodoroTab("Pausa corta", mode == 1) { mode = 1; running = false }
                PomodoroTab("Pausa larga", mode == 2) { mode = 2; running = false }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "%02d:%02d".format(remaining / 60, remaining % 60),
                color = colors.content,
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.subtleFill)
                        .clickable { running = !running }
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Text(if (running) "Pausar" else "Iniciar", color = colors.content)
                }
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Outlined.Refresh, "Reiniciar", tint = colors.content,
                    modifier = Modifier.size(24.dp).clickable {
                        running = false
                        remaining = totalSeconds
                    },
                )
            }
        }
    }
}

@Composable
private fun PomodoroTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (selected) Modifier.background(colors.subtleFill)
                else Modifier.border(1.dp, colors.stroke, RoundedCornerShape(10.dp))
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            color = if (selected) colors.content else colors.secondaryContent,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/* ============================ Time progress widget ============================ */

@Composable
fun TimeProgressWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    val now = remember { LocalDate.now() }
    val time = remember { java.time.LocalTime.now() }

    OasisCard(modifier = modifier) {
        Column {
            WidgetHeader(
                title = config.title.ifBlank { "Progreso del Tiempo" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
            )
            Spacer(Modifier.height(12.dp))
            if (config.showYear) ProgressRow("Progreso del Año", yearFraction(now))
            if (config.showMonth) ProgressRow("Progreso del Mes", monthFraction(now))
            if (config.showWeek) ProgressRow("Progreso de la Semana", weekFraction(now))
            if (config.showDay) ProgressRow("Progreso del Día", dayFraction(time))
        }
    }
}

@Composable
private fun ProgressRow(label: String, fraction: Float) {
    val colors = LocalEnfoqueColors.current
    val animated by animateFloatAsState(targetValue = fraction, label = "progress")
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = colors.content, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.subtleFill),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.content),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("${(animated * 100).toInt()}%", color = colors.secondaryContent, style = MaterialTheme.typography.bodyMedium)
    }
}

/* ============================ helpers ============================ */

private fun formatLongDate(date: LocalDate): String {
    val month = date.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
    return "${date.dayOfMonth} $month '${date.year % 100}"
}

private fun timeRange(ev: CalendarEvent): String {
    fun fmt(m: Int): String = "%02d:%02d".format(m / 60, m % 60)
    return if (ev.endMinutes >= 0) "${fmt(ev.startMinutes)} - ${fmt(ev.endMinutes)}" else fmt(ev.startMinutes)
}

private fun yearFraction(date: LocalDate): Float {
    val dayOfYear = date.dayOfYear
    val lengthOfYear = if (date.isLeapYear) 366 else 365
    return dayOfYear.toFloat() / lengthOfYear
}

private fun monthFraction(date: LocalDate): Float {
    val len = YearMonth.from(date).lengthOfMonth()
    return date.dayOfMonth.toFloat() / len
}

private fun weekFraction(date: LocalDate): Float {
    // Monday = 1 ... Sunday = 7
    val dow = date.dayOfWeek.value
    return dow.toFloat() / 7f
}

private fun dayFraction(time: java.time.LocalTime): Float {
    val secondsOfDay = time.toSecondOfDay()
    return secondsOfDay.toFloat() / (24 * 3600)
}

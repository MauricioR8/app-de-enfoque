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
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    var showConfig by remember { mutableStateOf(false) }
    val visibleTodos = if (config.hideCompleted) config.todos.filterNot { it.done } else config.todos
    val doneCount = config.todos.count { it.done }
    OasisCard(modifier = modifier) {
        Column {
            WidgetHeader(
                title = config.title.ifBlank { "Por hacer" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
                trailing = {
                    Icon(
                        Icons.Outlined.Tune, "Configurar", tint = colors.content,
                        modifier = Modifier.size(22.dp).clickable { showConfig = !showConfig },
                    )
                    Spacer(Modifier.width(8.dp))
                },
            )

            if (showConfig) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onChange(config.copy(hideCompleted = !config.hideCompleted)) },
                    ) {
                        Text("Ocultar completadas", color = colors.content, modifier = Modifier.weight(1f))
                        Text(if (config.hideCompleted) "Sí" else "No", color = colors.secondaryContent)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Limpiar completadas ($doneCount)",
                        color = colors.content,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, colors.stroke, RoundedCornerShape(50))
                            .clickable { onChange(config.copy(todos = config.todos.filterNot { it.done })) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            visibleTodos.forEach { todo ->
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { com.mauricior8.enfoque.data.CalendarRepository(context) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    val epochDay = date.toEpochDay()
    val dayEvents = config.events.filter { it.epochDay == epochDay }.sortedBy { it.startMinutes }

    var hasPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_CALENDAR,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var deviceEvents by remember { mutableStateOf<List<com.mauricior8.enfoque.data.DeviceCalendarEvent>>(emptyList()) }
    LaunchedEffect(date, hasPermission) {
        deviceEvents = if (hasPermission) {
            val start = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            repo.eventsForDay(start, end)
        } else emptyList()
    }

    OasisCard(modifier = modifier) {
        Column {
            WidgetHeader(
                title = config.title.ifBlank { "Calendario" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
                trailing = {
                    Icon(
                        Icons.Outlined.OpenInNew, "Abrir calendario", tint = colors.content,
                        modifier = Modifier.size(20.dp).clickable { openCalendar(context) },
                    )
                    Spacer(Modifier.width(8.dp))
                },
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

            if (!hasPermission) {
                Text(
                    "Conecta tu calendario (Google/Samsung) para ver tus eventos.",
                    color = colors.secondaryContent,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, colors.stroke, RoundedCornerShape(50))
                        .clickable { permLauncher.launch(android.Manifest.permission.READ_CALENDAR) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) { Text("Conectar calendario", color = colors.content) }
                Spacer(Modifier.height(12.dp))
            }

            // Device (synced) events
            deviceEvents.forEach { ev ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ev.title, color = colors.content, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (ev.allDay) "Todo el día" else deviceTimeRange(ev.beginMs, ev.endMs),
                            color = colors.secondaryContent,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // Local events (created here)
            if (dayEvents.isEmpty() && deviceEvents.isEmpty()) {
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
                                Text(timeRange(ev), color = colors.secondaryContent, style = MaterialTheme.typography.bodyMedium)
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
                                events = config.events + CalendarEvent(epochDay = epochDay, title = "Nuevo evento")
                            )
                        )
                    },
                )
            }
        }
    }
}

private fun openCalendar(context: android.content.Context) {
    val builder = android.provider.CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
    android.content.ContentUris.appendId(builder, System.currentTimeMillis())
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, builder.build())
        .apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
}

private fun deviceTimeRange(beginMs: Long, endMs: Long): String {
    val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return "${fmt.format(java.util.Date(beginMs))} - ${fmt.format(java.util.Date(endMs))}"
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
    val context = androidx.compose.ui.platform.LocalContext.current
    // mode: 0 = focus, 1 = short break, 2 = long break
    var mode by remember { mutableIntStateOf(0) }
    var showConfig by remember { mutableStateOf(false) }
    val totalSeconds = when (mode) {
        1 -> config.pomodoroShortBreak * 60
        2 -> config.pomodoroLongBreak * 60
        else -> config.pomodoroFocus * 60
    }
    var remaining by remember(mode, totalSeconds) { mutableIntStateOf(totalSeconds) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // Keep an alarm ringtone ready and clean it up.
    val ringtone = remember {
        runCatching {
            val uri = android.media.RingtoneManager.getActualDefaultRingtoneUri(context, android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            android.media.RingtoneManager.getRingtone(context, uri)
        }.getOrNull()
    }
    DisposableEffect(Unit) { onDispose { runCatching { ringtone?.stop() } } }

    LaunchedEffect(running, mode) {
        while (running && remaining > 0) {
            kotlinx.coroutines.delay(1000L)
            remaining -= 1
        }
        if (remaining <= 0 && running) {
            running = false
            finished = true
            if (config.pomodoroAlarm) runCatching { ringtone?.play() }
        }
    }

    OasisCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WidgetHeader(
                title = config.title.ifBlank { "Temporizador Pomodoro" },
                favorite = config.favorite,
                onToggleFavorite = { onChange(config.copy(favorite = !config.favorite)) },
                onRemove = onRemove,
                trailing = {
                    Icon(
                        Icons.Outlined.Tune, "Configurar", tint = colors.content,
                        modifier = Modifier.size(22.dp).clickable { showConfig = !showConfig },
                    )
                    Spacer(Modifier.width(8.dp))
                },
            )

            if (showConfig) {
                PomodoroConfig(config = config, onChange = onChange)
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PomodoroTab("Pomodoro", mode == 0) { mode = 0; running = false; finished = false }
                PomodoroTab("Pausa corta", mode == 1) { mode = 1; running = false; finished = false }
                PomodoroTab("Pausa larga", mode == 2) { mode = 2; running = false; finished = false }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "%02d:%02d".format(remaining / 60, remaining % 60),
                color = colors.content,
                style = MaterialTheme.typography.displayMedium,
            )
            if (finished) {
                Text("¡Tiempo!", color = colors.content, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.subtleFill)
                        .clickable {
                            if (finished) {
                                runCatching { ringtone?.stop() }
                                finished = false
                                remaining = totalSeconds
                            } else {
                                running = !running
                            }
                        }
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Text(if (finished) "Detener" else if (running) "Pausar" else "Iniciar", color = colors.content)
                }
                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Outlined.Refresh, "Reiniciar", tint = colors.content,
                    modifier = Modifier.size(24.dp).clickable {
                        runCatching { ringtone?.stop() }
                        running = false
                        finished = false
                        remaining = totalSeconds
                    },
                )
            }
        }
    }
}

@Composable
private fun PomodoroConfig(config: WidgetConfig, onChange: (WidgetConfig) -> Unit) {
    val colors = LocalEnfoqueColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text("Duraciones (minutos)", color = colors.content, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Stepper("Enfoque", config.pomodoroFocus) { onChange(config.copy(pomodoroFocus = it.coerceIn(1, 600))) }
        Stepper("Pausa corta", config.pomodoroShortBreak) { onChange(config.copy(pomodoroShortBreak = it.coerceIn(1, 120))) }
        Stepper("Pausa larga", config.pomodoroLongBreak) { onChange(config.copy(pomodoroLongBreak = it.coerceIn(1, 240))) }
        Spacer(Modifier.height(8.dp))
        Text("Atajos de enfoque", color = colors.secondaryContent, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(25 to "25 m", 60 to "1 h", 120 to "2 h", 240 to "4 h").forEach { (mins, label) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .then(if (config.pomodoroFocus == mins) Modifier.background(colors.subtleFill) else Modifier.border(1.dp, colors.stroke, RoundedCornerShape(50)))
                        .clickable { onChange(config.copy(pomodoroFocus = mins)) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) { Text(label, color = colors.content, style = MaterialTheme.typography.labelLarge) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onChange(config.copy(pomodoroAlarm = !config.pomodoroAlarm)) }) {
            Text("Sonar alarma al terminar", color = colors.content, modifier = Modifier.weight(1f))
            Text(if (config.pomodoroAlarm) "Sí" else "No", color = colors.secondaryContent)
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, onValue: (Int) -> Unit) {
    val colors = LocalEnfoqueColors.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = colors.content, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.Remove, "Menos", tint = colors.content, modifier = Modifier.size(26.dp).clickable { onValue(value - 5) })
        Text("$value", color = colors.content, modifier = Modifier.padding(horizontal = 14.dp))
        Icon(Icons.Outlined.Add, "Más", tint = colors.content, modifier = Modifier.size(26.dp).clickable { onValue(value + 5) })
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

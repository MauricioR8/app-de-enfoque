package com.mauricior8.enfoque.ui.oasis

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.mauricior8.enfoque.data.UsageStatsRepository
import com.mauricior8.enfoque.data.media.NowPlayingController
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.ui.components.OasisCard
import com.mauricior8.enfoque.ui.components.DrawableImage
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/* ===================== shared small header ===================== */

@Composable
internal fun SquareHeader(
    title: String,
    onRemove: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalEnfoqueColors.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = colors.content, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.invoke()
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Outlined.Close, "Quitar", tint = colors.secondaryContent,
            modifier = Modifier.size(20.dp).clickable { onRemove() },
        )
    }
}

@Composable
private fun Text(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) = androidx.compose.material3.Text(
    text = text, color = color, style = style, modifier = modifier,
    fontWeight = fontWeight, fontSize = fontSize, textAlign = textAlign, maxLines = maxLines,
)

@Composable
private fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) = androidx.compose.material3.Icon(imageVector, contentDescription, modifier, tint)

/* ===================== Quotes / Jokes ===================== */

@Composable
fun QuotesWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) = LinesWidget(
    config = config,
    lines = config.quotes,
    placeholderTitle = "Frases",
    inputHint = "Escribe una frase y púlsa +",
    emptyHint = "Añade tus frases favoritas y aparecerán aquí.",
    onLinesChange = { onChange(config.copy(quotes = it)) },
    onRemove = onRemove,
    modifier = modifier,
)

@Composable
fun JokesWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) = LinesWidget(
    config = config,
    lines = config.jokes,
    placeholderTitle = "Chistes",
    inputHint = "Escribe un chiste y púlsa +",
    emptyHint = "Añade chistes y se mostrarán uno a uno.",
    onLinesChange = { onChange(config.copy(jokes = it)) },
    onRemove = onRemove,
    modifier = modifier,
)

/** Shared implementation for the Quotes and Jokes cards. */
@Composable
private fun LinesWidget(
    config: WidgetConfig,
    lines: List<String>,
    placeholderTitle: String,
    inputHint: String,
    emptyHint: String,
    onLinesChange: (List<String>) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    var index by remember { mutableIntStateOf(0) }
    var draft by remember { mutableStateOf("") }
    val safeIndex = if (lines.isEmpty()) 0 else index.coerceIn(0, lines.size - 1)

    OasisCard(modifier = modifier) {
        Column {
            SquareHeader(config.title.ifBlank { placeholderTitle }, onRemove)
            Spacer(Modifier.height(12.dp))

            if (lines.isEmpty()) {
                Text(emptyHint, color = colors.secondaryContent)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "\u201C${lines[safeIndex]}\u201D",
                        color = colors.content,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.KeyboardArrowLeft, "Anterior", colors.content, Modifier.size(24.dp).clickable { if (safeIndex > 0) index = safeIndex - 1 })
                    Text("${safeIndex + 1}/${lines.size}", color = colors.secondaryContent, modifier = Modifier.padding(horizontal = 6.dp))
                    Icon(Icons.Outlined.KeyboardArrowRight, "Siguiente", colors.content, Modifier.size(24.dp).clickable { if (safeIndex < lines.size - 1) index = safeIndex + 1 })
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Eliminar",
                        color = colors.secondaryContent,
                        modifier = Modifier.clickable {
                            onLinesChange(lines.toMutableList().also { it.removeAt(safeIndex) })
                            index = 0
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    textStyle = TextStyle(color = colors.content, fontSize = 16.sp),
                    cursorBrush = SolidColor(colors.content),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) Text(inputHint, color = colors.secondaryContent, fontSize = 16.sp)
                        inner()
                    },
                )
                Icon(
                    Icons.Outlined.Add, "Añadir", colors.content,
                    Modifier.size(26.dp).clickable {
                        if (draft.isNotBlank()) {
                            onLinesChange(lines + draft.trim())
                            draft = ""
                            index = lines.size
                        }
                    },
                )
            }
        }
    }
}

/* ===================== App usage ===================== */

@Composable
fun AppUsageWidget(
    config: WidgetConfig,
    onChange: (WidgetConfig) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    val context = LocalContext.current
    val repo = remember { UsageStatsRepository(context) }
    var hasAccess by remember { mutableStateOf(repo.hasUsageAccess()) }
    var usage by remember { mutableStateOf<List<com.mauricior8.enfoque.data.AppUsage>>(emptyList()) }
    val windowDays = config.usageWindowDays

    LaunchedEffect(hasAccess, windowDays) {
        if (hasAccess) usage = repo.loadUsage(windowDays)
    }

    OasisCard(modifier = modifier) {
        Column {
            SquareHeader(config.title.ifBlank { "Uso de la App" }, onRemove)
            Spacer(Modifier.height(8.dp))

            if (!hasAccess) {
                Text("Se necesita permiso de \"Acceso de uso\" para mostrar tus estadísticas.", color = colors.secondaryContent)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, colors.stroke, RoundedCornerShape(50))
                        .clickable {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) { Text("Conceder permiso", color = colors.content) }
                Spacer(Modifier.height(6.dp))
                Text("Si ya lo concediste, vuelve a abrir esta pantalla.", color = colors.secondaryContent, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reintentar",
                    color = colors.content,
                    modifier = Modifier.clickable { hasAccess = repo.hasUsageAccess() },
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UsageTab("Últimas 24 h", windowDays == 1) { onChange(config.copy(usageWindowDays = 1)) }
                    UsageTab("Últimos 7 días", windowDays == 7) { onChange(config.copy(usageWindowDays = 7)) }
                }
                Spacer(Modifier.height(12.dp))
                if (usage.isEmpty()) {
                    Text("Sin datos todavía.", color = colors.secondaryContent)
                } else {
                    usage.forEach { u ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            DrawableImage(u.icon, u.label, Modifier.size(30.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(u.label, color = colors.content, maxLines = 1)
                                Text("${u.openCount} aperturas", color = colors.secondaryContent, fontSize = 12.sp)
                            }
                            Text(formatDuration(u.totalTimeMs), color = colors.content)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(if (selected) Modifier.background(colors.subtleFill) else Modifier.border(1.dp, colors.stroke, RoundedCornerShape(10.dp)))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) { Text(label, color = if (selected) colors.content else colors.secondaryContent) }
}

private fun formatDuration(ms: Long): String {
    val totalMin = ms / 60000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

/* ===================== Music ===================== */

@Composable
fun MusicWidget(
    config: WidgetConfig,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    val context = LocalContext.current
    val controller = remember { NowPlayingController(context) }
    var hasAccess by remember { mutableStateOf(controller.hasNotificationAccess()) }

    DisposableEffect(hasAccess) {
        if (hasAccess) controller.start()
        onDispose { controller.stop() }
    }

    val now = controller.state.value

    OasisCard(modifier = modifier) {
        Column {
            SquareHeader(config.title.ifBlank { "Música" }, onRemove)
            Spacer(Modifier.height(12.dp))

            if (!hasAccess) {
                Text("Para ver lo que suena, concede acceso a las notificaciones.", color = colors.secondaryContent)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, colors.stroke, RoundedCornerShape(50))
                        .clickable { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) { Text("Conceder acceso", color = colors.content) }
                Spacer(Modifier.height(6.dp))
                Text("Reintentar", color = colors.content, modifier = Modifier.clickable { hasAccess = controller.hasNotificationAccess() })
                Spacer(Modifier.height(8.dp))
                OpenMusicButton(context)
            } else if (now == null) {
                Text("No hay música sonando.", color = colors.secondaryContent)
                Spacer(Modifier.height(10.dp))
                OpenMusicButton(context)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val art = now.art
                    Box(
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(colors.subtleFill),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (art != null) {
                            Image(art.asImageBitmap(), "Álbum", Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Outlined.MusicNote, null, colors.secondaryContent, Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(now.title, color = colors.content, maxLines = 1, fontWeight = FontWeight.Medium)
                        if (now.artist.isNotBlank()) Text(now.artist, color = colors.secondaryContent, maxLines = 1)
                        if (now.album.isNotBlank()) Text(now.album, color = colors.secondaryContent, fontSize = 12.sp, maxLines = 1)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.SkipPrevious, "Anterior", colors.content, Modifier.size(32.dp).clickable { controller.previous() })
                    Icon(
                        if (now.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        "Reproducir/Pausar", colors.content,
                        Modifier.size(40.dp).clickable { controller.playPause() },
                    )
                    Icon(Icons.Outlined.SkipNext, "Siguiente", colors.content, Modifier.size(32.dp).clickable { controller.next() })
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                OpenMusicButton(context, now.packageName)
            }
        }
    }
}

@Composable
private fun OpenMusicButton(context: android.content.Context, preferredPackage: String? = null) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, colors.stroke, RoundedCornerShape(50))
            .clickable { openMusicApp(context, preferredPackage) }
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) { Text("Abrir música", color = colors.content) }
}

private fun openMusicApp(context: android.content.Context, preferredPackage: String?) {
    val pm = context.packageManager
    // Try the currently-playing app, then Samsung Music, then any music app.
    val candidates = listOfNotNull(preferredPackage, "com.sec.android.app.music", "com.samsung.android.app.music")
    for (pkg in candidates) {
        val intent = pm.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }
            return
        }
    }
    // Fallback: generic music intent.
    val music = Intent("android.intent.action.MUSIC_PLAYER").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(music) }
}

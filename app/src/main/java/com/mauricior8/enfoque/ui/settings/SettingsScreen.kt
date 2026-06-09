package com.mauricior8.enfoque.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mauricior8.enfoque.data.model.IconMode
import com.mauricior8.enfoque.ui.components.tappable
import com.mauricior8.enfoque.ui.theme.BackgroundPalette
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import com.mauricior8.enfoque.ui.theme.NamedColor

@Composable
fun SettingsScreen(
    iconMode: IconMode,
    backgroundColorId: String,
    launcherName: String,
    clock24h: Boolean,
    showSeconds: Boolean,
    arcBattery: Boolean,
    arcBatteryColor: Boolean,
    showRecentApps: Boolean,
    homeProgress: com.mauricior8.enfoque.data.model.ProgressSpan,
    onIconModeChange: (IconMode) -> Unit,
    onBackgroundColorChange: (String) -> Unit,
    onLauncherNameChange: (String) -> Unit,
    onClock24hChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onArcBatteryChange: (Boolean) -> Unit,
    onArcBatteryColorChange: (Boolean) -> Unit,
    onShowRecentAppsChange: (Boolean) -> Unit,
    onHomeProgressChange: (com.mauricior8.enfoque.data.model.ProgressSpan) -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.ArrowBack, "Volver", tint = colors.content,
                modifier = Modifier.size(26.dp).tappable(onBack),
            )
            Spacer(Modifier.width(16.dp))
            Text("Ajustes", color = colors.content, style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(28.dp))

        /* ---------- Launcher name ---------- */
        SectionTitle("Nombre en la pantalla de inicio")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.stroke, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            BasicTextField(
                value = launcherName,
                onValueChange = onLauncherNameChange,
                singleLine = true,
                textStyle = TextStyle(color = colors.content, fontSize = 16.sp),
                cursorBrush = SolidColor(colors.content),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (launcherName.isEmpty()) {
                        Text("(sin texto)", color = colors.secondaryContent)
                    }
                    inner()
                },
            )
        }
        Text(
            "Déjalo vacío para no mostrar ningún texto.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp),
        )
        Spacer(Modifier.height(28.dp))

        /* ---------- Clock ---------- */
        SectionTitle("Reloj")
        SwitchRow("Formato 24 horas", clock24h, onClock24hChange)
        SwitchRow("Mostrar segundos", showSeconds, onShowSecondsChange)
        SwitchRow("El círculo muestra la batería", arcBattery, onArcBatteryChange)
        SwitchRow("Colorear el círculo según la batería", arcBatteryColor, onArcBatteryColorChange)
        Text(
            "Consejo: mantén pulsado el círculo del reloj para abrir estos ajustes rápidamente.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(28.dp))

        /* ---------- Home progress bar ---------- */
        SectionTitle("Barra de progreso en inicio")
        Text(
            "Muestra una barra de progreso bajo el reloj. También puedes mantenerla pulsada en el inicio.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        val spans = com.mauricior8.enfoque.data.model.ProgressSpan.values()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            spans.toList().chunked(3).forEach { rowSpans ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowSpans.forEach { span ->
                        ChoiceChip(
                            label = progressLabel(span),
                            selected = homeProgress == span,
                            onClick = { onHomeProgressChange(span) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Pad the last row so chips keep equal width.
                    repeat(3 - rowSpans.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(28.dp))

        /* ---------- Icon mode ---------- */
        SectionTitle("Estilo de iconos")
        Text(
            "Cambia el aspecto de los iconos en el cajón, la búsqueda, la pantalla principal y las carpetas.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChoiceChip(
                label = "Color original",
                selected = iconMode == IconMode.COLOR,
                onClick = { onIconModeChange(IconMode.COLOR) },
                modifier = Modifier.weight(1f),
            )
            ChoiceChip(
                label = "Blanco y Negro",
                selected = iconMode == IconMode.BLACK_AND_WHITE,
                onClick = { onIconModeChange(IconMode.BLACK_AND_WHITE) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(28.dp))

        /* ---------- App drawer ---------- */
        SectionTitle("Cajón de aplicaciones")
        SwitchRow("Mostrar \"Instaladas recientemente\"", showRecentApps, onShowRecentAppsChange)
        Spacer(Modifier.height(28.dp))

        /* ---------- Background color ---------- */
        SectionTitle("Color de fondo")
        Text(
            "Negro y blanco puros, más 10 tonos relajados.",
            color = colors.secondaryContent,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        ColorPalette(
            selectedId = backgroundColorId,
            onSelect = onBackgroundColorChange,
        )
        Spacer(Modifier.height(28.dp))

        /* ---------- Default launcher ---------- */
        SectionTitle("Launcher predeterminado")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .border(1.dp, colors.stroke, RoundedCornerShape(50))
                .clickable { onSetDefaultLauncher() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Establecer como pantalla de inicio", color = colors.content)
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = LocalEnfoqueColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        // Minimalist toggle: outlined pill with a filled knob.
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(50))
                .border(1.dp, colors.stroke, RoundedCornerShape(50))
                .then(if (checked) Modifier.background(colors.subtleFill) else Modifier),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (checked) colors.content else colors.secondaryContent),
            )
        }
    }
}

/**
 * Quick clock-appearance dialog, opened by long-pressing the home-screen clock.
 */
@Composable
fun ClockSettingsDialog(
    clock24h: Boolean,
    showSeconds: Boolean,
    arcBattery: Boolean,
    onClock24hChange: (Boolean) -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onArcBatteryChange: (Boolean) -> Unit,
    onMoreSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.background)
                .border(1.dp, colors.stroke, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column {
                Text("Aspecto del reloj", color = colors.content, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                SwitchRow("Formato 24 horas", clock24h, onClock24hChange)
                SwitchRow("Mostrar segundos", showSeconds, onShowSecondsChange)
                SwitchRow("El círculo muestra la batería", arcBattery, onArcBatteryChange)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Más ajustes",
                    color = colors.content,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.subtleFill)
                        .clickable { onMoreSettings() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                )
            }
        }
    }
}

private fun progressLabel(span: com.mauricior8.enfoque.data.model.ProgressSpan): String = when (span) {
    com.mauricior8.enfoque.data.model.ProgressSpan.NONE -> "Ninguna"
    com.mauricior8.enfoque.data.model.ProgressSpan.DAY -> "Día"
    com.mauricior8.enfoque.data.model.ProgressSpan.WEEK -> "Semana"
    com.mauricior8.enfoque.data.model.ProgressSpan.MONTH -> "Mes"
    com.mauricior8.enfoque.data.model.ProgressSpan.YEAR -> "Año"
}

@Composable
private fun SectionTitle(text: String) {
    val colors = LocalEnfoqueColors.current
    Text(
        text = text,
        color = colors.content,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.background(colors.subtleFill)
                else Modifier.background(androidx.compose.ui.graphics.Color.Transparent)
            )
            .border(1.dp, if (selected) colors.content else colors.stroke, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = colors.content)
    }
}

@Composable
private fun ColorPalette(
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    val all: List<NamedColor> = BackgroundPalette.all

    // Use a non-scrolling wrapped row layout to avoid nested scroll conflicts.
    val rows = all.chunked(6)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rows.forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                rowColors.forEach { named ->
                    val isSelected = named.id == selectedId
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(named.color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.content else colors.stroke,
                                shape = CircleShape,
                            )
                            .clickable { onSelect(named.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            val tick = if (named.color.luminanceSafe() < 0.5f) {
                                androidx.compose.ui.graphics.Color.White
                            } else {
                                androidx.compose.ui.graphics.Color.Black
                            }
                            Icon(Icons.Outlined.Check, "Seleccionado", tint = tick, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.Color.luminanceSafe(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue

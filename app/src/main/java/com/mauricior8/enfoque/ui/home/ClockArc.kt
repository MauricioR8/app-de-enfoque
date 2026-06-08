package com.mauricior8.enfoque.ui.home

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centered digital clock framed by a thin circular arc. The arc can act either
 * as a minimalist frame or as a battery indicator that depletes as the charge
 * drops. The time/date are tappable and the whole clock is long-pressable to
 * open its appearance settings.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ClockArc(
    clock24h: Boolean,
    showSeconds: Boolean,
    arcBattery: Boolean,
    onClickTime: () -> Unit,
    onClickDate: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalEnfoqueColors.current

    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(if (showSeconds) 1000L else 5000L)
        }
    }

    val batteryFraction = rememberBatteryFraction()

    val timePattern = remember(clock24h, showSeconds) {
        val base = if (clock24h) "HH:mm" else "h:mm"
        if (showSeconds) "$base:ss" else base
    }
    val timeFormat = remember(timePattern) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val weekdayFormat = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    val dayMonthFormat = remember { SimpleDateFormat("d MMMM", Locale.getDefault()) }

    Box(
        modifier = modifier.combinedClickable(
            onClick = onClickTime,
            onLongClick = onLongPress,
        ),
        contentAlignment = Alignment.Center,
    ) {
        // The arc: a battery ring (depletes with charge) or a fixed frame.
        Canvas(modifier = Modifier.size(240.dp)) {
            val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width
            val topLeft = Offset(inset, inset)
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)

            if (arcBattery) {
                // Faint full track + bright depleting ring starting from the top.
                drawArc(
                    color = colors.content.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                drawArc(
                    color = colors.content,
                    startAngle = -90f,
                    sweepAngle = 360f * batteryFraction.coerceIn(0.02f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            } else {
                drawArc(
                    color = colors.content,
                    startAngle = -60f,
                    sweepAngle = 300f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            Text(
                text = timeFormat.format(now),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                color = colors.content,
                modifier = Modifier.combinedClickable(
                    onClick = onClickTime,
                    onLongClick = onLongPress,
                ),
            )
            // The day-of-week is shown a bit larger as a main detail.
            Text(
                text = weekdayFormat.format(now).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = colors.content,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = dayMonthFormat.format(now),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryContent,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .combinedClickable(onClick = onClickDate, onLongClick = onLongPress),
            )
            if (arcBattery) {
                Text(
                    text = "${(batteryFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryContent,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/** Observes the current battery level as a 0f..1f fraction. */
@Composable
private fun rememberBatteryFraction(): Float {
    val context = LocalContext.current
    var fraction by remember { mutableFloatStateOf(readBatteryFraction(context)) }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) fraction = level.toFloat() / scale.toFloat()
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return fraction
}

private fun readBatteryFraction(context: Context): Float {
    return runCatching {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (capacity in 0..100) capacity / 100f else 1f
    }.getOrDefault(1f)
}

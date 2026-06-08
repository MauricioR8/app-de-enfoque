package com.mauricior8.enfoque.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centered digital clock framed by a thin, incomplete circular arc — echoing the
 * reference launcher's home screen. The date is shown in a dimmed color below.
 */
@Composable
fun ClockArc(modifier: Modifier = Modifier) {
    val colors = LocalEnfoqueColors.current

    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // The incomplete arc acts as a minimalist frame around the clock.
        Canvas(modifier = Modifier.size(240.dp)) {
            val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width
            drawArc(
                color = colors.content,
                startAngle = -60f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = stroke,
            )
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
            )
            Text(
                text = dateFormat.format(now).replaceFirstChar { it.lowercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryContent,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

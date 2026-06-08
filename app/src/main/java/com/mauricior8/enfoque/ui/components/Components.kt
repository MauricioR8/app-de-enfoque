package com.mauricior8.enfoque.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mauricior8.enfoque.ui.theme.LocalEnfoqueColors

/**
 * The signature card used across the Oasis tools screen: a rectangle with a
 * very thin (1dp) border, transparent fill, rounded corners and no shadow.
 */
@Composable
fun OasisCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit,
) {
    val colors = LocalEnfoqueColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, colors.stroke), RoundedCornerShape(20.dp))
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * A pill-shaped button (fully rounded) with a hairline border, matching the
 * "+ Añadir widget" button from the reference.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    val colors = LocalEnfoqueColors.current
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (filled) Modifier.background(colors.subtleFill)
                else Modifier.border(BorderStroke(1.dp, colors.stroke), shape)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = colors.content)
    }
}

/** Convenience for a transparent clickable area with no ripple bleed. */
fun Modifier.tappable(onClick: () -> Unit): Modifier = this.composed {
    val interactionSource = androidx.compose.runtime.remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = onClick,
    )
}

internal val TransparentColor = Color.Transparent

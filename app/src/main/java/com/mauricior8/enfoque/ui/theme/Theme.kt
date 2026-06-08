package com.mauricior8.enfoque.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Holds the resolved background / content colors for the whole app so any
 * composable can read them, regardless of the user's selected wallpaper color.
 */
data class EnfoqueColors(
    val background: Color,
    val content: Color,
    val secondaryContent: Color,
    /** Hairline stroke color used for the card borders (1px white-ish lines). */
    val stroke: Color,
    /** Subtle fill used for pressed / selected states. */
    val subtleFill: Color,
)

val LocalEnfoqueColors = staticCompositionLocalOf {
    EnfoqueColors(
        background = PureBlack,
        content = PureWhite,
        secondaryContent = PureWhite.copy(alpha = 0.6f),
        stroke = PureWhite.copy(alpha = 0.5f),
        subtleFill = PureWhite.copy(alpha = 0.08f),
    )
}

/**
 * App theme. [backgroundColor] is the user-selected solid background; everything
 * else (text, icons, borders) adapts to keep good contrast.
 */
@Composable
fun AppDeEnfoqueTheme(
    backgroundColor: Color = PureBlack,
    content: @Composable () -> Unit,
) {
    val isDarkBackground = backgroundColor.luminance() < 0.5f
    val contentColor = if (isDarkBackground) PureWhite else PureBlack

    val enfoqueColors = EnfoqueColors(
        background = backgroundColor,
        content = contentColor,
        secondaryContent = contentColor.copy(alpha = 0.6f),
        stroke = contentColor.copy(alpha = 0.45f),
        subtleFill = contentColor.copy(alpha = 0.10f),
    )

    val colorScheme = if (isDarkBackground) {
        darkColorScheme(
            background = backgroundColor,
            surface = backgroundColor,
            onBackground = contentColor,
            onSurface = contentColor,
            primary = contentColor,
            onPrimary = backgroundColor,
        )
    } else {
        lightColorScheme(
            background = backgroundColor,
            surface = backgroundColor,
            onBackground = contentColor,
            onSurface = contentColor,
            primary = contentColor,
            onPrimary = backgroundColor,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context.findActivity()
        if (activity != null) {
            SideEffect {
                val window = activity.window
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                // Light system bars (dark icons) when the background is light.
                controller.isAppearanceLightStatusBars = !isDarkBackground
                controller.isAppearanceLightNavigationBars = !isDarkBackground
            }
        }
    }

    CompositionLocalProvider(LocalEnfoqueColors provides enfoqueColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

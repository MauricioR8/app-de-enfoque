package com.mauricior8.enfoque.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Pure base colors required by the spec.
 */
val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)

/**
 * A relaxed palette of 10 solid background colors (pastel / dark / neutral tones
 * that are easy on the eyes), as required by the design spec. These are shown
 * in Settings together with pure black and pure white.
 */
data class NamedColor(val id: String, val displayName: String, val color: Color)

object BackgroundPalette {

    val pureBlack = NamedColor("black", "Negro puro", PureBlack)
    val pureWhite = NamedColor("white", "Blanco puro", PureWhite)

    /** 10 extra relaxed solid colors. */
    val relaxed: List<NamedColor> = listOf(
        NamedColor("charcoal", "Carbón", Color(0xFF14171A)),
        NamedColor("slate", "Pizarra", Color(0xFF1E2A33)),
        NamedColor("midnight", "Medianoche", Color(0xFF12203A)),
        NamedColor("forest", "Bosque", Color(0xFF16241C)),
        NamedColor("plum", "Ciruela", Color(0xFF241726)),
        NamedColor("sand", "Arena", Color(0xFFEDE6D6)),
        NamedColor("sage", "Salvia", Color(0xFFD7E2D3)),
        NamedColor("mist", "Niebla", Color(0xFFD9E2E8)),
        NamedColor("blush", "Rubor", Color(0xFFEADADA)),
        NamedColor("latte", "Latte", Color(0xFFE3D7C9)),
    )

    /** All selectable background options, in display order. */
    val all: List<NamedColor> = listOf(pureBlack, pureWhite) + relaxed

    fun fromId(id: String?): NamedColor =
        all.firstOrNull { it.id == id } ?: pureBlack
}

/**
 * Returns the best foreground (text/icon) color for a given background,
 * keeping the ultra-minimalist black/white aesthetic.
 */
fun contentColorFor(background: Color): Color =
    if (background.luminance() < 0.5f) PureWhite else PureBlack

/**
 * A dimmed variant of the content color, used for secondary text such as the
 * date under the clock or hints. Alpha is applied over the content color.
 */
fun secondaryContentColorFor(background: Color): Color =
    contentColorFor(background).copy(alpha = 0.6f)

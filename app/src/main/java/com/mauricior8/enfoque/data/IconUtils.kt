package com.mauricior8.enfoque.data

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

/**
 * Helpers to render app icons either in their original color or as a true
 * grayscale version (same icon, only the colors are desaturated).
 */
object IconUtils {

    private const val ICON_SIZE_PX = 144

    /**
     * Returns a grayscale copy of [source] that keeps the icon's exact shape and
     * internal detail — only the colors are converted to gray. This is the
     * "Blanco y Negro" mode: the icon stays the same, just without color.
     */
    fun toGrayscale(source: Drawable, resources: Resources): Drawable {
        val width = source.intrinsicWidth.takeIf { it > 0 } ?: ICON_SIZE_PX
        val height = source.intrinsicHeight.takeIf { it > 0 } ?: ICON_SIZE_PX
        val bitmap = source.toBitmap(width, height, Bitmap.Config.ARGB_8888)

        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        // Desaturate completely while keeping luminance (and the alpha channel),
        // so the icon shape and shading are preserved.
        val matrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return BitmapDrawable(resources, output)
    }
}

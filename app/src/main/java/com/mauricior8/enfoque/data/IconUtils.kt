package com.mauricior8.enfoque.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

/**
 * Helpers to render app icons either in their original color or as clean
 * monochrome silhouettes for the "Black & White" mode.
 */
object IconUtils {

    private const val ICON_SIZE_PX = 144

    /**
     * Converts any drawable to a single-color silhouette using its alpha as a
     * mask, tinted with [tintColor]. This gives crisp white (or black) icons
     * that read well on the chosen background, as required by the spec.
     */
    fun toMonochrome(source: Drawable, tintColor: Int, resources: Resources): Drawable {
        val bitmap = source.toBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)

        // 1) Desaturate to grayscale to keep internal detail/contrast.
        val gray = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gray)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val saturation = ColorMatrix().apply { setSaturation(0f) }
        // Boost contrast a bit so faint icons stay visible.
        val contrast = contrastMatrix(1.25f)
        contrast.preConcat(saturation)
        paint.colorFilter = ColorMatrixColorFilter(contrast)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // 2) Tint using alpha mask so the icon takes a single readable color.
        val tinted = Bitmap.createBitmap(gray.width, gray.height, Bitmap.Config.ARGB_8888)
        val tintCanvas = Canvas(tinted)
        val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tintPaint.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        tintCanvas.drawBitmap(gray, 0f, 0f, tintPaint)

        return BitmapDrawable(resources, tinted)
    }

    private fun contrastMatrix(contrast: Float): ColorMatrix {
        val translate = (-.5f * contrast + .5f) * 255f
        return ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            )
        )
    }
}

internal val TRANSPARENT = Color.TRANSPARENT

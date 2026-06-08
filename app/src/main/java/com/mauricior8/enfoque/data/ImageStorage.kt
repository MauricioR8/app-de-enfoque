package com.mauricior8.enfoque.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Saves user-picked images (folder icons) into internal storage, center-cropping
 * them to a square and scaling them to the standard icon size so they match the
 * rest of the launcher icons.
 */
object ImageStorage {

    private const val TARGET_SIZE = 256

    /**
     * Reads the [source] image, crops it to a centered square, scales it to the
     * standard size and stores it as a PNG. Returns the absolute file path, or
     * null on failure.
     */
    fun saveCroppedSquare(context: Context, source: Uri, name: String): String? {
        return try {
            val original = context.contentResolver.openInputStream(source).use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null

            val side = min(original.width, original.height)
            val xOffset = (original.width - side) / 2
            val yOffset = (original.height - side) / 2
            val squared = Bitmap.createBitmap(original, xOffset, yOffset, side, side)
            val scaled = Bitmap.createScaledBitmap(squared, TARGET_SIZE, TARGET_SIZE, true)

            val dir = File(context.filesDir, "folder_icons").apply { mkdirs() }
            val file = File(dir, "$name.png")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            if (squared != original) original.recycle()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}

package com.retrobreeze.ribbonlauncher.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.core.graphics.alpha
import androidx.core.graphics.get
import androidx.core.graphics.createBitmap

fun isIconLikelyCircular(drawable: Drawable): Boolean {
    val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return false
    val width = bitmap.width
    val height = bitmap.height
    val left = bitmap[0, height / 2]
    val right = bitmap[width - 1, height / 2]
    val top = bitmap[width / 2, 0]
    val bottom = bitmap[width / 2, height - 1]

    return left == 0 && right == 0 && top == 0 && bottom == 0
}


private fun Drawable.toBitmapSafe(width: Int, height: Int): Bitmap {
    return when (this) {
        is BitmapDrawable -> this.bitmap
        is AdaptiveIconDrawable -> {
            val bitmap = createBitmap(width, height)
            val canvas = android.graphics.Canvas(bitmap)
            this.setBounds(0, 0, width, height)
            this.draw(canvas)
            bitmap
        }
        else -> {
            val bitmap = createBitmap(width, height)
            val canvas = android.graphics.Canvas(bitmap)
            this.setBounds(0, 0, width, height)
            this.draw(canvas)
            bitmap
        }
    }
}

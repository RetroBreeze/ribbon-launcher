package com.retrobreeze.ribbonlauncher.model

import android.graphics.drawable.Drawable

data class GameEntry(
    val packageName: String,
    val displayName: String,
    val icon: Drawable
)

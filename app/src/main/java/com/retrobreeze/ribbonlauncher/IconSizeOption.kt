package com.retrobreeze.ribbonlauncher

enum class IconSizeOption(val multiplier: Float) {
    FULL(1f),
    THREE_QUARTERS(0.75f),
    HALF(0.5f);

    fun next(): IconSizeOption = when (this) {
        FULL -> THREE_QUARTERS
        THREE_QUARTERS -> HALF
        HALF -> FULL
    }
}

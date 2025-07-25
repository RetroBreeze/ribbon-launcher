package com.retrobreeze.ribbonlauncher.model

/**
 * Stores per-app customization such as custom label, icon and wallpaper URIs.
 */
data class AppCustomization(
    var label: String? = null,
    var iconUri: String? = null,
    var wallpaperUri: String? = null
)

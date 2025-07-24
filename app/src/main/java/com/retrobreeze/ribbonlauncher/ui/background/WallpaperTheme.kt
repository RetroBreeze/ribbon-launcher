package com.retrobreeze.ribbonlauncher.ui.background

import androidx.compose.ui.graphics.Color

enum class WallpaperTheme(val label: String, val startColor: Color, val endColor: Color) {
    XMB_CLASSIC_BLUE("XMB Classic Blue", Color(0xFF20408F), Color(0xFF0A0F2C)),
    ELECTRIC_VIOLET("Electric Violet", Color(0xFF8F00FF), Color(0xFF1C0036)),
    NEON_TEAL("Neon Teal", Color(0xFF00C9A7), Color(0xFF003D3A)),
    CRIMSON_FADE("Crimson Fade", Color(0xFFB00020), Color(0xFF2C0000)),
    SUNSET_GOLD("Sunset Gold", Color(0xFFFFA726), Color(0xFF6A1B09)),
    ROYAL_INDIGO("Royal Indigo", Color(0xFF3F51B5), Color(0xFF1A237E)),
    EMERALD_NIGHT("Emerald Night", Color(0xFF2E7D32), Color(0xFF00210F)),
    GRAPHITE_GRAY("Graphite Gray", Color(0xFF424242), Color(0xFF121212)),
    BUBBLEGUM_POP("Bubblegum Pop", Color(0xFFFF77A9), Color(0xFF6A1B47)),
    MIDNIGHT_SKY("Midnight Sky", Color(0xFF1A2980), Color(0xFF000000));
}

package com.retrobreeze.ribbonlauncher

enum class SortMode(val label: String) {
    AZ("A-Z"),
    ZA("Z-A"),
    RECENT("Recent");

    fun next(): SortMode = when (this) {
        AZ -> ZA
        ZA -> RECENT
        RECENT -> AZ
    }
}

package com.retrobreeze.ribbonlauncher

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.launcherDataStore by preferencesDataStore("launcher_prefs")

object LauncherPreferences {
    val KEY_SORT_MODE = stringPreferencesKey("sort_mode")
    val KEY_SELECTED_GAME = stringPreferencesKey("selected_game")
    val KEY_LAST_PLAYED = stringPreferencesKey("last_played")
}

private fun encodeLastPlayed(map: Map<String, Long>): String =
    map.entries.joinToString(";") { "${it.key}:${it.value}" }

private fun decodeLastPlayed(str: String): Map<String, Long> =
    str.split(';').mapNotNull {
        val parts = it.split(':')
        if (parts.size == 2) parts[0] to parts[1].toLongOrNull() else null
    }.toMap()

suspend fun Context.saveSortMode(mode: SortMode) {
    launcherDataStore.edit { it[LauncherPreferences.KEY_SORT_MODE] = mode.name }
}

suspend fun Context.loadSortMode(): SortMode {
    val prefs = launcherDataStore.data.first()
    return prefs[LauncherPreferences.KEY_SORT_MODE]?.let { runCatching { SortMode.valueOf(it) }.getOrNull() } ?: SortMode.AZ
}

suspend fun Context.saveSelectedGame(packageName: String?) {
    launcherDataStore.edit { prefs ->
        if (packageName == null) prefs.remove(LauncherPreferences.KEY_SELECTED_GAME)
        else prefs[LauncherPreferences.KEY_SELECTED_GAME] = packageName
    }
}

suspend fun Context.loadSelectedGame(): String? {
    val prefs = launcherDataStore.data.first()
    return prefs[LauncherPreferences.KEY_SELECTED_GAME]
}

suspend fun Context.saveLastPlayed(map: Map<String, Long>) {
    launcherDataStore.edit { it[LauncherPreferences.KEY_LAST_PLAYED] = encodeLastPlayed(map) }
}

suspend fun Context.loadLastPlayed(): Map<String, Long> {
    val prefs = launcherDataStore.data.first()
    val str = prefs[LauncherPreferences.KEY_LAST_PLAYED] ?: return emptyMap()
    return decodeLastPlayed(str)
}

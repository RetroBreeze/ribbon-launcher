package com.retrobreeze.ribbonlauncher

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.os.Build
import com.retrobreeze.ribbonlauncher.model.GameEntry

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val KEY_LAST_PLAYED_PREFIX = "lp_"
        private const val KEY_SELECTED_GAME = "selected_game"
        private const val KEY_ENABLED_PACKAGES = "enabled_packages"
    }

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var allGames: List<GameEntry> = emptyList()

    var games by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    var apps by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    var enabledPackages by mutableStateOf<Set<String>>(emptySet())
        private set

    var sortMode by mutableStateOf(SortMode.AZ)
        private set

    private val lastPlayed = mutableStateMapOf<String, Long>()

    var selectedGamePackage by mutableStateOf<String?>(null)
        private set

    init {
        loadPreferences()
        loadInstalledGames()
        loadInstalledApps()
    }

    private fun loadPreferences() {
        sortMode = try {
            SortMode.valueOf(prefs.getString(KEY_SORT_MODE, SortMode.AZ.name)!!)
        } catch (_: IllegalArgumentException) {
            SortMode.AZ
        }

        selectedGamePackage = prefs.getString(KEY_SELECTED_GAME, null)

        enabledPackages = prefs.getStringSet(KEY_ENABLED_PACKAGES, emptySet()) ?: emptySet()

        prefs.all.forEach { (key, value) ->
            if (key.startsWith(KEY_LAST_PLAYED_PREFIX)) {
                val packageName = key.removePrefix(KEY_LAST_PLAYED_PREFIX)
                val time = when (value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: return@forEach
                    else -> return@forEach
                }
                lastPlayed[packageName] = time
            }
        }
    }

    private fun loadInstalledGames() {
        val context = getApplication<Application>().applicationContext
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(intent, 0)

        allGames = resolveInfoList
            .map { it.activityInfo.applicationInfo }
            .filter { appInfo ->
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME
                } else true

                val isNotSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0

                isGame && isNotSystemApp
            }


            .map { appInfo ->
                GameEntry(
                    packageName = appInfo.packageName,
                    displayName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            }

        sortGames()
    }

    private fun loadInstalledApps() {
        val context = getApplication<Application>().applicationContext
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(intent, 0)

        apps = resolveInfoList
            .map { it.activityInfo.applicationInfo }
            .filter { appInfo ->
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME
                } else false

                val isNotSystemUid = appInfo.uid >= android.os.Process.FIRST_APPLICATION_UID
                val isNotSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) != null

                !isGame && isNotSystemApp && isNotSystemUid && launchIntent
            }
            .map { appInfo ->
                GameEntry(
                    packageName = appInfo.packageName,
                    displayName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            }

        sortGames()
    }

    fun cycleSortMode() {
        sortMode = sortMode.next()
        prefs.edit().putString(KEY_SORT_MODE, sortMode.name).apply()

        sortGames()
    }

    fun setSelectedGame(packageName: String?) {
        selectedGamePackage = packageName
        with(prefs.edit()) {
            if (packageName == null) remove(KEY_SELECTED_GAME)
            else putString(KEY_SELECTED_GAME, packageName)
            apply()
        }
    }

    fun recordLaunch(game: GameEntry) {
        val now = System.currentTimeMillis()
        lastPlayed[game.packageName] = now
        prefs.edit().putLong(KEY_LAST_PLAYED_PREFIX + game.packageName, now).apply()
    }

    fun refreshSort() {
        sortGames()
    }

    fun setPackageEnabled(packageName: String, enabled: Boolean) {
        enabledPackages = enabledPackages.toMutableSet().apply {
            if (enabled) add(packageName) else remove(packageName)
        }
        prefs.edit().putStringSet(KEY_ENABLED_PACKAGES, enabledPackages).apply()
        sortGames()
    }

    fun selectAll() {
        enabledPackages = (allGames + apps).map { it.packageName }.toSet()
        prefs.edit().putStringSet(KEY_ENABLED_PACKAGES, enabledPackages).apply()
        sortGames()
    }

    fun selectNone() {
        enabledPackages = emptySet()
        prefs.edit().putStringSet(KEY_ENABLED_PACKAGES, enabledPackages).apply()
        sortGames()
    }

    fun getAllInstalledApps(): List<GameEntry> {
        return (allGames + apps).sortedBy { it.displayName.lowercase() }
    }

    private fun sortGames() {
        if (enabledPackages.isEmpty()) {
            enabledPackages = allGames.map { it.packageName }.toSet()
            prefs.edit().putStringSet(KEY_ENABLED_PACKAGES, enabledPackages).apply()
        }

        val allEntries = (allGames + apps)
        val selectedEntries = allEntries.filter { enabledPackages.contains(it.packageName) }

        games = when (sortMode) {
            SortMode.AZ -> selectedEntries.sortedBy { it.displayName.lowercase() }
            SortMode.ZA -> selectedEntries.sortedByDescending { it.displayName.lowercase() }
            SortMode.RECENT -> selectedEntries.sortedWith(
                compareByDescending<GameEntry> { lastPlayed[it.packageName] ?: Long.MIN_VALUE }
                    .thenBy { it.displayName.lowercase() }
            )
        }
    }

}

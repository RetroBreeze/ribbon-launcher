package com.retrobreeze.ribbonlauncher

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.retrobreeze.ribbonlauncher.model.GameEntry

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val KEY_LAST_PLAYED_PREFIX = "lp_"
    }

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var allGames: List<GameEntry> = emptyList()

    var games by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    var apps by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    var sortMode by mutableStateOf(SortMode.AZ)
        private set

    var selectedGamePackageName by mutableStateOf<String?>(null)
        private set

    private val lastPlayed = mutableStateMapOf<String, Long>()

    init {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            sortMode = context.loadSortMode()
            selectedGamePackageName = context.loadSelectedGame()
            lastPlayed.putAll(context.loadLastPlayed())
            loadInstalledGames()
            loadInstalledApps()
        }

    }

    private fun loadPreferences() {
        sortMode = try {
            SortMode.valueOf(prefs.getString(KEY_SORT_MODE, SortMode.AZ.name)!!)
        } catch (_: IllegalArgumentException) {
            SortMode.AZ
        }

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
    }

    fun cycleSortMode() {
        sortMode = sortMode.next()
        viewModelScope.launch {
            getApplication<Application>().applicationContext.saveSortMode(sortMode)
        }

        sortGames()
    }

    fun recordLaunch(game: GameEntry) {
        lastPlayed[game.packageName] = System.currentTimeMillis()
        viewModelScope.launch {
            getApplication<Application>().applicationContext.saveLastPlayed(lastPlayed)
        }

        if (sortMode == SortMode.RECENT) {
            sortGames()
        }
    }

    fun setSelectedGame(packageName: String?) {
        selectedGamePackageName = packageName
        viewModelScope.launch {
            getApplication<Application>().applicationContext.saveSelectedGame(packageName)
        }
    }

    private fun sortGames() {
        games = when (sortMode) {
            SortMode.AZ -> allGames.sortedBy { it.displayName.lowercase() }
            SortMode.ZA -> allGames.sortedByDescending { it.displayName.lowercase() }
            SortMode.RECENT -> allGames.sortedWith(
                compareByDescending<GameEntry> { lastPlayed[it.packageName] ?: Long.MIN_VALUE }
                    .thenBy { it.displayName.lowercase() }
            )
        }
    }

}

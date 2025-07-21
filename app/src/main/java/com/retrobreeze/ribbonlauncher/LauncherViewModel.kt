package com.retrobreeze.ribbonlauncher

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.os.Build
import com.retrobreeze.ribbonlauncher.model.GameEntry

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    private var allGames: List<GameEntry> = emptyList()

    var games by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    var apps by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    var sortMode by mutableStateOf(SortMode.AZ)
        private set

    private val lastPlayed = mutableStateMapOf<String, Long>()

    init {
        loadInstalledGames()
        loadInstalledApps()
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
        sortGames()
    }

    fun recordLaunch(game: GameEntry) {
        lastPlayed[game.packageName] = System.currentTimeMillis()
        if (sortMode == SortMode.RECENT) {
            sortGames()
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

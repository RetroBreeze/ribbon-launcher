package com.retrobreeze.ribbonlauncher

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.os.Build
import com.retrobreeze.ribbonlauncher.model.GameEntry

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    var games by mutableStateOf<List<GameEntry>>(emptyList())
        private set

    init {
        loadInstalledGames()
    }

    private fun loadInstalledGames() {
        val context = getApplication<Application>().applicationContext
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(intent, 0)

        games = resolveInfoList
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
    }

}

package com.retrobreeze.ribbonlauncher

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.os.Build
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.model.AppCustomization
import com.retrobreeze.ribbonlauncher.ui.background.WallpaperTheme

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PREFS_NAME = "launcher_prefs"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val KEY_LAST_PLAYED_PREFIX = "lp_"
        private const val KEY_SELECTED_GAME = "selected_game"
        private const val KEY_ENABLED_PACKAGES = "enabled_packages"
        private const val KEY_RIBBON_TITLE = "ribbon_title"
        private const val KEY_ICON_SIZE = "icon_size"
        private const val KEY_SHOW_LABELS = "show_labels"
        private const val KEY_WALLPAPER_THEME = "wallpaper_theme"
        private const val KEY_SETTINGS_LOCKED = "settings_locked"
        private const val KEY_CUSTOM_LABEL_PREFIX = "custom_label_"
        private const val KEY_CUSTOM_ICON_PREFIX = "custom_icon_"
        private const val KEY_CUSTOM_WALLPAPER_PREFIX = "custom_wallpaper_"
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

    var ribbonTitle by mutableStateOf("Games")
        private set

    var iconSizeOption by mutableStateOf(IconSizeOption.FULL)
        private set

    var showLabels by mutableStateOf(true)
        private set

    var wallpaperTheme by mutableStateOf(WallpaperTheme.XMB_CLASSIC_BLUE)
        private set

    var settingsLocked by mutableStateOf(false)
        private set

    private val customizations = mutableStateMapOf<String, AppCustomization>()

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

        ribbonTitle = prefs.getString(KEY_RIBBON_TITLE, "Games") ?: "Games"

        selectedGamePackage = prefs.getString(KEY_SELECTED_GAME, null)

        iconSizeOption = try {
            IconSizeOption.valueOf(prefs.getString(KEY_ICON_SIZE, IconSizeOption.FULL.name)!!)
        } catch (_: IllegalArgumentException) {
            IconSizeOption.FULL
        }

        wallpaperTheme = try {
            WallpaperTheme.valueOf(prefs.getString(KEY_WALLPAPER_THEME, WallpaperTheme.XMB_CLASSIC_BLUE.name)!!)
        } catch (_: IllegalArgumentException) {
            WallpaperTheme.XMB_CLASSIC_BLUE
        }

        showLabels = prefs.getBoolean(KEY_SHOW_LABELS, true)

        settingsLocked = prefs.getBoolean(KEY_SETTINGS_LOCKED, false)

        enabledPackages = if (prefs.contains(KEY_ENABLED_PACKAGES)) {
            prefs.getStringSet(KEY_ENABLED_PACKAGES, emptySet())?.toSet() ?: emptySet()
        } else {
            emptySet()
        }

        prefs.all.forEach { (key, value) ->
            when {
                key.startsWith(KEY_LAST_PLAYED_PREFIX) -> {
                    val packageName = key.removePrefix(KEY_LAST_PLAYED_PREFIX)
                    val time = when (value) {
                        is Long -> value
                        is String -> value.toLongOrNull() ?: return@forEach
                        else -> return@forEach
                    }
                    lastPlayed[packageName] = time
                }
                key.startsWith(KEY_CUSTOM_LABEL_PREFIX) -> {
                    val pkg = key.removePrefix(KEY_CUSTOM_LABEL_PREFIX)
                    customizations.getOrPut(pkg) { AppCustomization() }.label = value as? String
                }
                key.startsWith(KEY_CUSTOM_ICON_PREFIX) -> {
                    val pkg = key.removePrefix(KEY_CUSTOM_ICON_PREFIX)
                    customizations.getOrPut(pkg) { AppCustomization() }.iconUri = value as? String
                }
                key.startsWith(KEY_CUSTOM_WALLPAPER_PREFIX) -> {
                    val pkg = key.removePrefix(KEY_CUSTOM_WALLPAPER_PREFIX)
                    customizations.getOrPut(pkg) { AppCustomization() }.wallpaperUri = value as? String
                }
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
                val pkg = appInfo.packageName
                val customization = customizations[pkg]
                val label = customization?.label ?: pm.getApplicationLabel(appInfo).toString()
                val icon = customization?.iconUri?.let { loadDrawable(it) } ?: pm.getApplicationIcon(appInfo)
                GameEntry(
                    packageName = pkg,
                    displayName = label,
                    icon = icon
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
                val pkg = appInfo.packageName
                val customization = customizations[pkg]
                val label = customization?.label ?: pm.getApplicationLabel(appInfo).toString()
                val icon = customization?.iconUri?.let { loadDrawable(it) } ?: pm.getApplicationIcon(appInfo)
                GameEntry(
                    packageName = pkg,
                    displayName = label,
                    icon = icon
                )
            }

        sortGames()
    }

    fun cycleSortMode() {
        sortMode = sortMode.next()
        prefs.edit().putString(KEY_SORT_MODE, sortMode.name).apply()

        sortGames()
    }

    fun cycleIconSize() {
        iconSizeOption = iconSizeOption.next()
        prefs.edit().putString(KEY_ICON_SIZE, iconSizeOption.name).apply()
    }

    fun toggleShowLabels() {
        showLabels = !showLabels
        prefs.edit().putBoolean(KEY_SHOW_LABELS, showLabels).apply()
    }

    fun updateSortMode(mode: SortMode) {
        sortMode = mode
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

    fun updateRibbonTitle(title: String) {
        ribbonTitle = title
        prefs.edit().putString(KEY_RIBBON_TITLE, title).apply()
    }

    fun updateWallpaperTheme(theme: WallpaperTheme) {
        wallpaperTheme = theme
        prefs.edit().putString(KEY_WALLPAPER_THEME, theme.name).apply()
    }

    fun toggleSettingsLocked() {
        settingsLocked = !settingsLocked
        prefs.edit().putBoolean(KEY_SETTINGS_LOCKED, settingsLocked).apply()
    }

    fun updateCustomLabel(packageName: String, label: String?) {
        if (label.isNullOrEmpty()) {
            customizations[packageName]?.label = null
            prefs.edit().remove(KEY_CUSTOM_LABEL_PREFIX + packageName).apply()
        } else {
            customizations.getOrPut(packageName) { AppCustomization() }.label = label
            prefs.edit().putString(KEY_CUSTOM_LABEL_PREFIX + packageName, label).apply()
        }
        loadInstalledGames()
        loadInstalledApps()
    }

    fun updateCustomIcon(packageName: String, uri: String?) {
        if (uri.isNullOrEmpty()) {
            customizations[packageName]?.iconUri = null
            prefs.edit().remove(KEY_CUSTOM_ICON_PREFIX + packageName).apply()
        } else {
            customizations.getOrPut(packageName) { AppCustomization() }.iconUri = uri
            prefs.edit().putString(KEY_CUSTOM_ICON_PREFIX + packageName, uri).apply()
        }
        loadInstalledGames()
        loadInstalledApps()
    }

    fun updateCustomWallpaper(packageName: String, uri: String?) {
        if (uri.isNullOrEmpty()) {
            customizations[packageName]?.wallpaperUri = null
            prefs.edit().remove(KEY_CUSTOM_WALLPAPER_PREFIX + packageName).apply()
        } else {
            customizations.getOrPut(packageName) { AppCustomization() }.wallpaperUri = uri
            prefs.edit().putString(KEY_CUSTOM_WALLPAPER_PREFIX + packageName, uri).apply()
        }
    }

    fun getCustomization(packageName: String?): AppCustomization? {
        return packageName?.let { customizations[it] }
    }

    fun resetLauncher() {
        prefs.edit().clear().apply()
        lastPlayed.clear()
        loadPreferences()
        sortGames()
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

    fun updateEnabledPackages(packages: Set<String>) {
        enabledPackages = packages.toSet()
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

    fun selectGames() {
        val gamePackages = allGames.map { it.packageName }
        enabledPackages = enabledPackages.toMutableSet().apply {
            clear()
            addAll(gamePackages)
        }
        prefs.edit().putStringSet(KEY_ENABLED_PACKAGES, enabledPackages).apply()
        sortGames()
    }

    fun getAllInstalledApps(): List<GameEntry> {
        return (allGames + apps).sortedBy { it.displayName.lowercase() }
    }

    fun getInstalledGames(): List<GameEntry> {
        return allGames
    }

    private fun loadDrawable(uriString: String): Drawable? {
        val context = getApplication<Application>().applicationContext
        return try {
            val uri = Uri.parse(uriString)
            val input = context.contentResolver.openInputStream(uri)
            input?.use {
                val bitmap = BitmapFactory.decodeStream(it)
                BitmapDrawable(context.resources, bitmap)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun sortGames() {
        if (!prefs.contains(KEY_ENABLED_PACKAGES) && enabledPackages.isEmpty()) {
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

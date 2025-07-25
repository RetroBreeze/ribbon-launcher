package com.retrobreeze.ribbonlauncher

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.os.Build
import com.retrobreeze.ribbonlauncher.model.GameEntry
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
        private const val KEY_PINNED_PACKAGES = "pinned_packages"
        private const val KEY_CUSTOM_ICON_PREFIX = "custom_icon_"
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

    var pinnedPackages by mutableStateOf<List<String>>(emptyList())
        private set

    private val customIcons = mutableMapOf<String, Drawable>()

    val visiblePinnedCount: Int
        get() = pinnedPackages.count { enabledPackages.contains(it) }

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

        pinnedPackages = prefs.getString(KEY_PINNED_PACKAGES, "")
            ?.takeIf { it.isNotEmpty() }
            ?.split(',') ?: emptyList()

        enabledPackages = if (prefs.contains(KEY_ENABLED_PACKAGES)) {
            prefs.getStringSet(KEY_ENABLED_PACKAGES, emptySet())?.toSet() ?: emptySet()
        } else {
            emptySet()
        }

        val context = getApplication<Application>().applicationContext
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
                key.startsWith(KEY_CUSTOM_ICON_PREFIX) -> {
                    val packageName = key.removePrefix(KEY_CUSTOM_ICON_PREFIX)
                    val uriString = value as? String ?: return@forEach
                    loadCustomIcon(context, packageName, uriString)
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
                GameEntry(
                    packageName = appInfo.packageName,
                    displayName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            }

        applyCustomIcons()
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

        applyCustomIcons()
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

    fun resetLauncher() {
        prefs.edit().clear().apply()
        lastPlayed.clear()
        customIcons.clear()
        pinnedPackages = emptyList()
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

    fun togglePin(packageName: String) {
        pinnedPackages = pinnedPackages.toMutableList().apply {
            if (contains(packageName)) remove(packageName) else add(packageName)
        }
        prefs.edit().putString(KEY_PINNED_PACKAGES, pinnedPackages.joinToString(",")).apply()
        sortGames()
    }

    fun updateCustomIcon(packageName: String, uri: Uri) {
        val context = getApplication<Application>().applicationContext
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val drawable = Drawable.createFromStream(stream, uri.toString()) ?: return
                customIcons[packageName] = drawable
                prefs.edit().putString(KEY_CUSTOM_ICON_PREFIX + packageName, uri.toString()).apply()
                allGames = allGames.map { if (it.packageName == packageName) it.copy(icon = drawable) else it }
                apps = apps.map { if (it.packageName == packageName) it.copy(icon = drawable) else it }
                sortGames()
            }
        } catch (_: Exception) {
        }
    }

    private fun applyCustomIcons() {
        if (customIcons.isEmpty()) return
        allGames = allGames.map { entry ->
            customIcons[entry.packageName]?.let { entry.copy(icon = it) } ?: entry
        }
        apps = apps.map { entry ->
            customIcons[entry.packageName]?.let { entry.copy(icon = it) } ?: entry
        }
    }

    fun getAllInstalledApps(): List<GameEntry> {
        return (allGames + apps).sortedBy { it.displayName.lowercase() }
    }

    fun getInstalledGames(): List<GameEntry> {
        return allGames
    }

    private fun loadCustomIcon(context: Context, packageName: String, uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val drawable = Drawable.createFromStream(stream, uri.toString()) ?: return
                customIcons[packageName] = drawable
            }
        } catch (_: Exception) {
        }
    }

    private fun sortGames() {
        if (!prefs.contains(KEY_ENABLED_PACKAGES) && enabledPackages.isEmpty()) {
            enabledPackages = allGames.map { it.packageName }.toSet()
            prefs.edit().putStringSet(KEY_ENABLED_PACKAGES, enabledPackages).apply()
        }

        val allEntries = (allGames + apps)
        val selectedEntries = allEntries.filter { enabledPackages.contains(it.packageName) }

        val pinned = pinnedPackages.mapNotNull { pkg ->
            selectedEntries.find { it.packageName == pkg }
        }
        val unpinned = selectedEntries.filterNot { pinnedPackages.contains(it.packageName) }

        val sortedUnpinned = when (sortMode) {
            SortMode.AZ -> unpinned.sortedBy { it.displayName.lowercase() }
            SortMode.ZA -> unpinned.sortedByDescending { it.displayName.lowercase() }
            SortMode.RECENT -> unpinned.sortedWith(
                compareByDescending<GameEntry> { lastPlayed[it.packageName] ?: Long.MIN_VALUE }
                    .thenBy { it.displayName.lowercase() }
            )
        }

        games = pinned + sortedUnpinned
    }

}

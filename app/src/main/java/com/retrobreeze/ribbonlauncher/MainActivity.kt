package com.retrobreeze.ribbonlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.retrobreeze.ribbonlauncher.GameCarousel
import com.retrobreeze.ribbonlauncher.SettingsMenu
import com.retrobreeze.ribbonlauncher.RibbonTitle
import com.retrobreeze.ribbonlauncher.StatusTopBar
import com.retrobreeze.ribbonlauncher.NavigationBottomBar
import com.retrobreeze.ribbonlauncher.EditAppsDialog
import com.retrobreeze.ribbonlauncher.WallpaperThemeDialog
import com.retrobreeze.ribbonlauncher.ResetConfirmationDialog
import com.retrobreeze.ribbonlauncher.ui.background.AnimatedBackground
import com.retrobreeze.ribbonlauncher.ui.theme.RibbonLauncherTheme

class MainActivity : ComponentActivity() {
    private val launcherViewModel: LauncherViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            RibbonLauncherTheme {
                LauncherScreen(viewModel = launcherViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel.refreshSort()
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(viewModel: LauncherViewModel = viewModel()) {
    val games = viewModel.games
    val sortMode = viewModel.sortMode
    val apps = viewModel.apps
    val context = LocalContext.current
    var showDrawer by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(initialPage = 0) { games.size + if (!viewModel.settingsLocked) 1 else 0 }

    LaunchedEffect(pagerState.currentPage, games) {
        val pkg = games.getOrNull(pagerState.currentPage)?.packageName
        viewModel.setSelectedGame(pkg)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedBackground(
                theme = viewModel.wallpaperTheme,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                GameCarousel(
                    games = games,
                    pagerState = pagerState,
                    selectedPackageName = viewModel.selectedGamePackage,
                    iconScale = viewModel.iconSizeOption.multiplier,
                    showLabels = viewModel.showLabels,
                    showEditButton = !viewModel.settingsLocked,
                    onLaunch = { game ->
                        val intent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                        if (intent != null) {
                            context.startActivity(intent)
                        } else {
                            val uri = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                        viewModel.recordLaunch(game)
                    },
                    onEdit = { showEditDialog = true }
                )
            }
            AppDrawerOverlay(
                apps = apps,
                showDrawer = showDrawer,
                onDismiss = { showDrawer = false },
                modifier = Modifier.align(Alignment.Center)
            )
            EditAppsDialog(
                show = showEditDialog,
                allApps = viewModel.getAllInstalledApps(),
                games = viewModel.getInstalledGames(),
                selectedPackages = viewModel.enabledPackages,
                onConfirm = { packages -> viewModel.updateEnabledPackages(packages) },
                onDismiss = { showEditDialog = false }
            )
            WallpaperThemeDialog(
                show = showWallpaperDialog,
                current = viewModel.wallpaperTheme,
                onSelect = { viewModel.updateWallpaperTheme(it) },
                onDismiss = { showWallpaperDialog = false }
            )
            ResetConfirmationDialog(
                show = showResetDialog,
                onConfirm = {
                    viewModel.resetLauncher()
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RibbonTitle(
                    title = viewModel.ribbonTitle,
                    onTitleChange = { viewModel.updateRibbonTitle(it) },
                    enabled = !viewModel.settingsLocked
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Spacer(Modifier.width(8.dp))
                SettingsMenu(
                    sortMode = sortMode,
                    onSortClick = { viewModel.cycleSortMode() },
                    onIconSizeClick = { viewModel.cycleIconSize() },
                    showLabels = viewModel.showLabels,
                    onToggleLabels = { viewModel.toggleShowLabels() },
                    onWallpaperClick = { showWallpaperDialog = true },
                    locked = viewModel.settingsLocked,
                    onLockToggle = { viewModel.toggleSettingsLocked() },
                    onResetClick = { showResetDialog = true }
                )
            }
            StatusTopBar(modifier = Modifier.align(Alignment.TopCenter))
            NavigationBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp),
                onRightClick = { showDrawer = !showDrawer }
            )
        }

    }
}
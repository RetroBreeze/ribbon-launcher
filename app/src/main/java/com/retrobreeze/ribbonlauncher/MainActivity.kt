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
import com.retrobreeze.ribbonlauncher.GameCarousel
import com.retrobreeze.ribbonlauncher.SortButton
import com.retrobreeze.ribbonlauncher.StatusTopBar
import com.retrobreeze.ribbonlauncher.NavigationBottomBar
import com.retrobreeze.ribbonlauncher.EditAppsDialog
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
    val pagerState = rememberPagerState(initialPage = 0) { games.size + 1 }

    LaunchedEffect(pagerState.currentPage, games) {
        val pkg = games.getOrNull(pagerState.currentPage)?.packageName
        viewModel.setSelectedGame(pkg)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedBackground(modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp, bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                GameCarousel(
                    games = games,
                    pagerState = pagerState,
                    selectedPackageName = viewModel.selectedGamePackage,
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
                selectedPackages = viewModel.enabledPackages,
                onPackageChecked = { pkg, checked -> viewModel.setPackageEnabled(pkg, checked) },
                onSelectAll = { viewModel.selectAll() },
                onSelectNone = { viewModel.selectNone() },
                onDismiss = { showEditDialog = false }
            )
            SortButton(
                sortMode = sortMode,
                onClick = {
                    viewModel.cycleSortMode()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 36.dp)
            )
            StatusTopBar(modifier = Modifier.align(Alignment.TopCenter))
            NavigationBottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                onRightClick = { showDrawer = !showDrawer }
            )
        }

    }
}
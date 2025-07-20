package com.retrobreeze.ribbonlauncher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrobreeze.ribbonlauncher.GameCarousel
import com.retrobreeze.ribbonlauncher.StatusTopBar
import com.retrobreeze.ribbonlauncher.ui.theme.RibbonLauncherTheme

class MainActivity : ComponentActivity() {
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
                LauncherScreen()
            }
        }
    }
}


@Composable
fun LauncherScreen(viewModel: LauncherViewModel = viewModel()) {
    val games = viewModel.games
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                GameCarousel(games) { game ->
                    val intent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    } else {
                        val uri = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }
            }
            StatusTopBar(modifier = Modifier.align(Alignment.TopCenter))
        }

    }
}
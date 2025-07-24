package com.retrobreeze.ribbonlauncher.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun Color.contrastingColor(): Color = if (luminance() < 0.5f) Color.White else Color.Black

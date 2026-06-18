package com.pdm0126.overload.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Overload es nativamente oscuro como el universo de DC
private val DarkColorScheme = darkColorScheme(
    primary = OverloadRed,
    onPrimary = OverloadWhite,
    secondary = OverloadMediumGray,
    onSecondary = OverloadWhite,
    tertiary = OverloadGold,
    background = OverloadDarkGray,
    onBackground = OverloadWhite,
    surface = OverloadDarkGray,
    onSurface = OverloadWhite,
    surfaceVariant = OverloadMediumGray,
    onSurfaceVariant = OverloadWhite
)

@Composable
fun OverloadTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
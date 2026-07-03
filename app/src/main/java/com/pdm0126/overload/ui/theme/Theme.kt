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


    primaryContainer = OverloadRedBright,
    onPrimaryContainer = OverloadWhite,

    secondary = OverloadLightGray,
    onSecondary = OverloadBlack,

    tertiary = OverloadGold,
    onTertiary = OverloadBlack,

    background = OverloadBlack,
    onBackground = OverloadWhite,

    surface = OverloadDarkGray,
    onSurface = OverloadWhite,

    surfaceVariant = OverloadMediumGray,
    onSurfaceVariant = OverloadLightGray
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
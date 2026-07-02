package com.pdm0126.overload.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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
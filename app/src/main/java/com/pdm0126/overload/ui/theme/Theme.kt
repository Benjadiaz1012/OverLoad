package com.pdm0126.overload.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OverloadGold,
    onPrimary = OverloadBlack,
    primaryContainer = OverloadGoldDim,
    onPrimaryContainer = OverloadWhite,

    secondary = OverloadOnSurfaceVariant,
    onSecondary = OverloadBlack,

    tertiary = OverloadGold,
    onTertiary = OverloadBlack,

    error = OverloadError,
    onError = OverloadWhite,
    errorContainer = OverloadErrorContainer,
    onErrorContainer = OverloadWhite,

    background = OverloadBackground,
    onBackground = OverloadWhite,

    surface = OverloadSurface,
    onSurface = OverloadWhite,

    surfaceVariant = OverloadSurfaceVariant,
    onSurfaceVariant = OverloadOnSurfaceVariant,

    outline = OverloadDivider,
    outlineVariant = OverloadDivider
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
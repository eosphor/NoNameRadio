package com.eosphor.nonameradio.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary,
    onPrimaryContainer = Color.White,
    secondary = Accent,
    onSecondary = Color.White,
    secondaryContainer = Accent,
    onSecondaryContainer = Color.White,
    tertiary = TabUnderline,
    onTertiary = Color.White,
    background = WindowBackground,
    onBackground = MenuTextDefault,
    surface = PlayerBackground,
    onSurface = MenuTextDefault,
    surfaceVariant = TagBackground,
    onSurfaceVariant = MenuTextDefault,
    outline = PlayerShadow,
    error = SwipeDeleteBackground,
    onError = SwipeDeleteIcon
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = TextColorPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextColorPrimary,
    secondary = AccentDark,
    onSecondary = TextColorPrimary,
    secondaryContainer = AccentDark,
    onSecondaryContainer = TextColorPrimary,
    tertiary = TabUnderlineDark,
    onTertiary = TextColorPrimary,
    background = WindowBackgroundDark,
    onBackground = MenuTextDefaultDark,
    surface = PlayerBackgroundDark,
    onSurface = MenuTextDefaultDark,
    surfaceVariant = TagBackgroundDark,
    onSurfaceVariant = MenuTextDefaultDark,
    outline = PlayerShadowDark,
    error = SwipeDeleteBackgroundDark,
    onError = SwipeDeleteIconDark
)

@Composable
fun RadioDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
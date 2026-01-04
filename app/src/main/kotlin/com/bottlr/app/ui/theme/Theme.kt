package com.bottlr.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Amber/Whiskey theme colors
private val AmberPrimary = Color(0xFFFFC107)
private val AmberDark = Color(0xFFC79100)
private val AmberLight = Color(0xFFFFECB3)
private val OnAmber = Color(0xFF000000)

// Teal secondary colors
private val TealPrimary = Color(0xFF018786)
private val TealLight = Color(0xFF03DAC5)

// Surface colors
private val SurfaceDark = Color(0xFF121212)
private val SurfaceDarkVariant = Color(0xFF1E1E1E)
private val SurfaceDarkElevated = Color(0xFF2D2D2D)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceLightVariant = Color(0xFFF5F5F5)

// Text colors
private val NearBlack = Color(0xFF202020)
private val NearWhite = Color(0xFFF8F8F8)

// Error colors
private val ErrorDark = Color(0xFFCF6679)
private val ErrorLight = Color(0xFFB00020)

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = OnAmber,
    primaryContainer = AmberDark,
    onPrimaryContainer = AmberLight,
    secondary = TealLight,
    onSecondary = Color.Black,
    secondaryContainer = TealPrimary,
    onSecondaryContainer = TealLight,
    tertiary = AmberLight,
    onTertiary = OnAmber,
    background = SurfaceDark,
    onBackground = NearWhite,
    surface = SurfaceDark,
    onSurface = NearWhite,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = SurfaceDark,
    surfaceContainerLow = SurfaceDarkVariant,
    surfaceContainer = SurfaceDarkVariant,
    surfaceContainerHigh = SurfaceDarkElevated,
    surfaceContainerHighest = SurfaceDarkElevated,
    error = ErrorDark,
    onError = Color.Black,
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = AmberDark,
    onPrimary = OnAmber,
    primaryContainer = AmberLight,
    onPrimaryContainer = AmberDark,
    secondary = TealPrimary,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealPrimary,
    tertiary = AmberDark,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = NearBlack,
    surface = SurfaceLight,
    onSurface = NearBlack,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceLightVariant,
    surfaceContainer = SurfaceLightVariant,
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0E9),
    error = ErrorLight,
    onError = Color.White,
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun BottlrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

package com.bottlr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import com.bottlr.app.navigation.BottlrNavHost
import com.bottlr.app.ui.theme.BottlrTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Container for Compose UI.
 *
 * Uses Jetpack Compose with Material3 theming.
 * Navigation handled by Compose Navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme preference before super.onCreate()
        applyThemeFromPreferences()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeMode = getThemeModeFromPreferences()

        setContent {
            val darkTheme = rememberDarkTheme(themeMode)
            BottlrTheme(darkTheme = darkTheme) {
                BottlrNavHost()
            }
        }
    }

    private fun applyThemeFromPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)

        val nightMode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun getThemeModeFromPreferences(): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    companion object {
        const val PREFS_NAME = "bottlr_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }
}

@Composable
private fun rememberDarkTheme(themeMode: Int): Boolean {
    val isSystemDark = isSystemInDarkTheme()
    return remember(themeMode, isSystemDark) {
        when (themeMode) {
            MainActivity.THEME_LIGHT -> false
            MainActivity.THEME_DARK -> true
            else -> isSystemDark
        }
    }
}

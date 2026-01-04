package com.bottlr.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Simplified container for Navigation Component.
 *
 * All UI logic is in Fragments and ViewModels following MVVM architecture.
 * Navigation uses custom slide-out menu, not the standard ActionBar.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme preference before super.onCreate() and setContentView()
        applyThemeFromPreferences()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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

    companion object {
        const val PREFS_NAME = "bottlr_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }
}

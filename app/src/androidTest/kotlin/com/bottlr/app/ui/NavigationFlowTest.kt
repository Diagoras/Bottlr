package com.bottlr.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import com.bottlr.app.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for basic navigation flows.
 *
 * Tests cover:
 * - App launches successfully
 * - Home screen displays correctly
 * - Navigation menu opens and navigates to gallery
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appLaunches_homeScreenDisplayed() {
        // Verify the app title is displayed
        onView(withId(R.id.Title_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Bottlr")))
    }

    @Test
    fun menuButton_opensNavigationDrawer() {
        // Click the menu button
        onView(withId(R.id.menu_icon))
            .perform(click())

        // Verify the navigation menu is visible (check for a nav item)
        onView(withId(R.id.menu_liquorcab_button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigationToGallery_displaysGalleryScreen() {
        // Open menu
        onView(withId(R.id.menu_icon))
            .perform(click())

        // Click Liquor Cabinet
        onView(withId(R.id.menu_liquorcab_button))
            .perform(click())

        // Verify we're on the gallery screen (check for the RecyclerView)
        onView(withId(R.id.liquorRecycler))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigationToCocktails_displaysCocktailScreen() {
        // Open menu
        onView(withId(R.id.menu_icon))
            .perform(click())

        // Click Cocktail Menu
        onView(withId(R.id.menu_cocktail_button))
            .perform(click())

        // Verify we're on the cocktail gallery screen (uses same RecyclerView ID)
        onView(withId(R.id.liquorRecycler))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigationToSettings_displaysSettingsScreen() {
        // Open menu
        onView(withId(R.id.menu_icon))
            .perform(click())

        // Click Settings
        onView(withId(R.id.menu_settings_button))
            .perform(click())

        // Verify we're on settings (check for login button or user text)
        onView(withId(R.id.signed_in_user))
            .check(matches(isDisplayed()))
    }

    @Test
    fun galleryScreen_menuButtonOpensNavWindow() {
        // Navigate to gallery first
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_liquorcab_button)).perform(click())

        // Verify we're on gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))

        // Open menu from gallery screen
        onView(withId(R.id.menu_icon)).perform(click())

        // Verify nav menu is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))
    }

    @Test
    fun galleryScreen_canNavigateToHomeViaMenu() {
        // Navigate to gallery
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_liquorcab_button)).perform(click())

        // Open menu and go home
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_home_button)).perform(click())

        // Verify we're back on home (title displayed)
        onView(withId(R.id.Title_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Bottlr")))
    }

    @Test
    fun galleryScreen_canNavigateToSettingsViaMenu() {
        // Navigate to gallery
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_liquorcab_button)).perform(click())

        // Open menu and go to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify we're on settings
        onView(withId(R.id.signed_in_user)).check(matches(isDisplayed()))
    }

    @Test
    fun settingsScreen_menuButtonOpensNavWindow() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify we're on settings
        onView(withId(R.id.signed_in_user)).check(matches(isDisplayed()))

        // Open menu from settings screen
        onView(withId(R.id.menu_icon)).perform(click())

        // Verify nav menu is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))
    }

    @Test
    fun settingsScreen_canNavigateToHomeViaMenu() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Open menu and go home
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_home_button)).perform(click())

        // Verify we're back on home
        onView(withId(R.id.Title_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Bottlr")))
    }
}

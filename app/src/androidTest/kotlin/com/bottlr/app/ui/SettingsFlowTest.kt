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
 * UI tests for Settings screen and sync functionality.
 *
 * Note: Actual Firebase sync cannot be tested in UI tests without mocking.
 * These tests verify the UI flow and button visibility.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun settingsScreen_displaysUserStatus() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify user status text is displayed
        onView(withId(R.id.signed_in_user))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsScreen_showsLoginOrLogoutButton() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Login button should be visible (signed out state in tests)
        onView(withId(R.id.login_Button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsScreen_feedbackButtonDisplayed() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify feedback button is displayed
        onView(withId(R.id.feedback_Button))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsScreen_versionTextDisplayed() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify version text is displayed
        onView(withId(R.id.versionText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun settingsScreen_syncButtonClickable() {
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Click login first (sync button only visible when logged in)
        // Since we can't actually sign in during tests, just verify the button exists
        // This test catches XML onClick crashes - if android:onClick is in XML,
        // clicking will crash with "Could not find method onClick(View)"
        onView(withId(R.id.login_Button))
            .check(matches(isDisplayed()))
            .perform(click())

        // The Google sign-in dialog will appear, but clicking login_Button
        // itself tests that the button's click handler works without crashing
    }

    @Test
    fun settingsScreen_canNavigateAwayViaMenu() {
        // This tests the fix for menu not working from settings screen
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify settings screen
        onView(withId(R.id.signed_in_user)).check(matches(isDisplayed()))

        // Navigate away via menu (tests nav window works from settings)
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_home_button)).perform(click())

        // Verify we navigated successfully
        onView(withId(R.id.Title_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Bottlr")))
    }

    @Test
    fun settingsScreen_menuNavigationWorksAfterVisitingSettings() {
        // This tests the fix for menu not working from settings screen
        // Navigate to settings
        onView(withId(R.id.menu_icon)).perform(click())
        onView(withId(R.id.menu_settings_button)).perform(click())

        // Verify settings screen
        onView(withId(R.id.signed_in_user)).check(matches(isDisplayed()))

        // Open menu from settings
        onView(withId(R.id.menu_icon)).perform(click())

        // Navigate to liquor cabinet
        onView(withId(R.id.menu_liquorcab_button)).perform(click())

        // Verify navigation succeeded
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
    }
}

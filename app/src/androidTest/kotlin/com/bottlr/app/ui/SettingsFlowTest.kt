package com.bottlr.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for Settings screen using Compose.
 *
 * Note: Actual Firebase sync cannot be tested in UI tests without mocking.
 * These tests verify the UI flow and element visibility.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun navigateToSettings() {
        // Wait for home screen to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Menu")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()

        // Wait for drawer to open
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("drawer_Settings")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("drawer_Settings").performClick()
        composeTestRule.waitForIdle()

        // Wait for settings screen to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Account")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun settingsScreen_displaysAccountSection() {
        navigateToSettings()

        // Verify account section is displayed
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        // Auth state varies in tests - check for either signed in or signed out state
        val notSignedIn = composeTestRule.onAllNodes(hasText("Not signed in")).fetchSemanticsNodes().isNotEmpty()
        val signedIn = composeTestRule.onAllNodes(hasText("Sign Out")).fetchSemanticsNodes().isNotEmpty()
        assert(notSignedIn || signedIn) { "Expected either 'Not signed in' or 'Sign Out' to be present" }
    }

    @Test
    fun settingsScreen_showsAuthState() {
        navigateToSettings()

        // Firebase auth state varies in tests - check for either state
        val hasSignIn = composeTestRule.onAllNodes(hasText("Sign in with Google")).fetchSemanticsNodes().isNotEmpty()
        val hasSignOut = composeTestRule.onAllNodes(hasText("Sign Out")).fetchSemanticsNodes().isNotEmpty()

        // At least one auth-related element should be present
        assert(hasSignIn || hasSignOut) { "Expected either Sign in or Sign out button" }
    }

    @Test
    fun settingsScreen_themeOptionsDisplayed() {
        navigateToSettings()

        // Verify theme section is displayed
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("System default").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_canSelectTheme() {
        navigateToSettings()

        // Select Dark theme
        composeTestRule.onNodeWithText("Dark").performClick()
        composeTestRule.waitForIdle()

        // Dark option should still be visible (theme applied)
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()

        // Select Light theme
        composeTestRule.onNodeWithText("Light").performClick()
        composeTestRule.waitForIdle()

        // Light option should still be visible
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_backButtonWorks() {
        navigateToSettings()

        // Verify we're on settings
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()

        // Click back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Verify we're back on home
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_canNavigateToGalleryViaDrawer() {
        navigateToSettings()

        // Verify settings screen
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()

        // Navigate via back button first (settings doesn't have drawer)
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Now use drawer from home
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()

        // Verify navigation succeeded
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_authButtonClickable() {
        navigateToSettings()

        // Firebase auth state varies - test whichever button is present
        val hasSignIn = composeTestRule.onAllNodes(hasText("Sign in with Google")).fetchSemanticsNodes().isNotEmpty()
        val hasSignOut = composeTestRule.onAllNodes(hasText("Sign Out")).fetchSemanticsNodes().isNotEmpty()

        if (hasSignIn) {
            // Click sign in button
            composeTestRule.onNodeWithText("Sign in with Google").performClick()
            composeTestRule.waitForIdle()
        } else if (hasSignOut) {
            // Click sign out button
            composeTestRule.onNodeWithText("Sign Out").performClick()
            composeTestRule.waitForIdle()
        } else {
            // No auth button found - fail test
            assert(false) { "Expected either Sign in or Sign out button" }
        }

        // If we get here without crashing, the button click worked
    }

    @Test
    fun settingsScreen_navigationAfterSettings() {
        // Navigate to settings
        navigateToSettings()

        // Verify settings screen
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()

        // Go back to home
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Verify we can navigate to other screens after visiting settings
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drawer_Cocktails").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on cocktails screen
        composeTestRule.onNodeWithText("Search cocktails...").assertIsDisplayed()
    }
}

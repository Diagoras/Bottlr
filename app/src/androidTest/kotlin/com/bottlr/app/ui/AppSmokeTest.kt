package com.bottlr.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests to verify the app launches without crashing.
 *
 * These tests catch critical issues like:
 * - Compose UI rendering errors
 * - Hilt injection failures
 * - Navigation setup errors
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_withHomeScreenDisplayed() {
        // Verify home screen unique elements
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun app_launches_withStatsCardsDisplayed() {
        // Verify stats cards are present (by checking for the unit text)
        // The count display format is "X bottles" or "X cocktails"
        composeTestRule.onNodeWithText("bottle", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("cocktail", substring = true).assertIsDisplayed()
    }

    @Test
    fun app_launches_withQuickActionsDisplayed() {
        // Verify quick action buttons are visible
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Bottle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Cocktail").assertIsDisplayed()
    }
}

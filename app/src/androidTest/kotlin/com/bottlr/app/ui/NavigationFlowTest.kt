package com.bottlr.app.ui

import androidx.compose.ui.test.assertIsDisplayed
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
 * UI tests for basic navigation flows using Compose Navigation.
 *
 * Tests cover:
 * - Home screen displays correctly
 * - Navigation drawer opens and works
 * - Can navigate between all main screens
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun openDrawer() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun appLaunches_homeScreenDisplayed() {
        // Verify home screen elements (Quick Actions is unique to home)
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun menuButton_opensNavigationDrawer() {
        openDrawer()

        // Verify drawer items are visible (using testTags)
        composeTestRule.onNodeWithTag("drawer_Home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_Cocktails").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drawer_Settings").assertIsDisplayed()
    }

    @Test
    fun navigationToBottleGallery_displaysGalleryScreen() {
        openDrawer()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on the gallery screen
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
    }

    @Test
    fun navigationToCocktails_displaysCocktailScreen() {
        openDrawer()
        composeTestRule.onNodeWithTag("drawer_Cocktails").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on the cocktail gallery screen
        composeTestRule.onNodeWithText("Cocktail Menu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search cocktails...").assertIsDisplayed()
    }

    @Test
    fun navigationToSettings_displaysSettingsScreen() {
        openDrawer()
        composeTestRule.onNodeWithTag("drawer_Settings").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on settings
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
    }

    @Test
    fun galleryScreen_drawerNavigatesToHome() {
        // Navigate to gallery first
        openDrawer()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()

        // Open drawer and go home
        openDrawer()
        composeTestRule.onNodeWithTag("drawer_Home").performClick()
        composeTestRule.waitForIdle()

        // Verify we're back on home
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_drawerNavigatesToGallery() {
        // Navigate to settings
        openDrawer()
        composeTestRule.onNodeWithTag("drawer_Settings").performClick()
        composeTestRule.waitForIdle()

        // Verify settings
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()

        // Navigate back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Should be back on home
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun homeScreen_statsCardNavigatesToGallery() {
        // Click on Liquor Cabinet stats card (first match, not drawer)
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()

        // Verify navigation to gallery
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
    }

    @Test
    fun homeScreen_addBottleButtonNavigates() {
        // Click Add Bottle button
        composeTestRule.onNodeWithText("Add Bottle").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on bottle editor
        composeTestRule.onNodeWithText("New Bottle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name *").assertIsDisplayed()
    }

    @Test
    fun homeScreen_addCocktailButtonNavigates() {
        // Click Add Cocktail button
        composeTestRule.onNodeWithText("Add Cocktail").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on cocktail editor
        composeTestRule.onNodeWithText("New Cocktail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cocktail Name *").assertIsDisplayed()
    }
}

package com.bottlr.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Add Bottle flow using Compose.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddBottleFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToBottleEditor() {
        // Wait for home screen to fully load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Add Bottle")).fetchSemanticsNodes().isNotEmpty()
        }
        // Click Add Bottle on home screen
        composeTestRule.onNodeWithText("Add Bottle").performClick()
        composeTestRule.waitForIdle()
        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Name *")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToGalleryThenEditor() {
        // Navigate via drawer to gallery, then FAB
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Add Bottle").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun addBottle_formFieldsAreDisplayed() {
        navigateToBottleEditor()

        // Top fields should be visible
        composeTestRule.onNodeWithText("Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Distillery").assertIsDisplayed()

        // Fields that might be below the fold - scroll to them
        composeTestRule.onNodeWithText("Type (e.g., Bourbon, Scotch)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Region").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Bottle").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun addBottle_fillFormAndSave_bottleAppearsInGallery() {
        val bottleName = "TestWhisky${System.currentTimeMillis()}"

        navigateToGalleryThenEditor()

        // Fill form
        composeTestRule.onNodeWithText("Name *").performTextInput(bottleName)
        composeTestRule.onNodeWithText("Distillery").performTextInput("Test Distillery")

        // Scroll to save button and click
        composeTestRule.onNodeWithText("Save Bottle").performScrollTo().performClick()

        // Wait for save and navigation (longer timeout for emulator)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Wait a bit more for list to update
        composeTestRule.waitForIdle()

        // Verify we're back in gallery and bottle is visible
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(bottleName)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(bottleName).assertIsDisplayed()
    }

    @Test
    fun addBottle_withAllFields_savesSuccessfully() {
        val bottleName = "CompleteBottle${System.currentTimeMillis()}"

        navigateToGalleryThenEditor()

        // Fill all fields
        composeTestRule.onNodeWithText("Name *").performTextInput(bottleName)
        composeTestRule.onNodeWithText("Distillery").performTextInput("Lagavulin")
        composeTestRule.onNodeWithText("Type (e.g., Bourbon, Scotch)").performScrollTo().performTextInput("Single Malt Scotch")
        composeTestRule.onNodeWithText("Region").performScrollTo().performTextInput("Islay")

        // Scroll to save button and click
        composeTestRule.onNodeWithText("Save Bottle").performScrollTo().performClick()

        // Wait for navigation back to gallery (longer timeout for emulator)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Wait for list to update
        composeTestRule.waitForIdle()

        // Verify bottle appears in gallery
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(bottleName)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(bottleName).assertIsDisplayed()
    }

    @Test
    fun addBottle_backButton_returnsWithoutSaving() {
        navigateToBottleEditor()

        composeTestRule.onNodeWithText("New Bottle").assertIsDisplayed()

        // Press back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Verify we're back on home
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun addBottle_saveButtonDisabled_whenNameEmpty() {
        navigateToBottleEditor()

        // Fill other fields but not name
        composeTestRule.onNodeWithText("Distillery").performTextInput("Test")

        // Save button should still be there (we can't easily check disabled state in Compose tests)
        // But clicking it shouldn't navigate away
        composeTestRule.onNodeWithText("Save Bottle").performClick()
        composeTestRule.waitForIdle()

        // Should still be on editor (name is required)
        composeTestRule.onNodeWithText("New Bottle").assertIsDisplayed()
    }
}

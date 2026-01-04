package com.bottlr.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
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
 * UI tests for viewing and editing bottles using Compose.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ViewEditBottleFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToGallery() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()
    }

    private fun createTestBottle(): String {
        val bottleName = "EditTest${System.currentTimeMillis()}"

        // Navigate to gallery and create bottle
        navigateToGallery()
        composeTestRule.onNodeWithContentDescription("Add Bottle").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Name *")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Name *").performTextInput(bottleName)
        composeTestRule.onNodeWithText("Distillery").performTextInput("Original Distillery")

        // Scroll to save button and click
        composeTestRule.onNodeWithText("Save Bottle").performScrollTo().performClick()

        // Wait for navigation back to gallery (longer timeout for emulator)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Wait for bottle to appear in list
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(bottleName)).fetchSemanticsNodes().isNotEmpty()
        }

        return bottleName
    }

    @Test
    fun gallery_displaysSearchField() {
        navigateToGallery()
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
    }

    @Test
    fun gallery_fabIsVisible() {
        navigateToGallery()
        composeTestRule.onNodeWithContentDescription("Add Bottle").assertIsDisplayed()
    }

    @Test
    fun viewBottle_displaysDetails() {
        val name = createTestBottle()

        // Tap on bottle
        composeTestRule.onNodeWithText(name).performClick()
        composeTestRule.waitForIdle()

        // Wait for details to load
        waitForDetailsToLoad()

        // Verify details screen - name appears twice (title bar and content), so check at least one exists
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(name)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Original Distillery").assertIsDisplayed()
    }

    private fun waitForDetailsToLoad() {
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Edit")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun editBottle_changesAreSaved() {
        val name = createTestBottle()
        val updatedName = "Updated$name"

        // Navigate to details
        composeTestRule.onNodeWithText(name).performClick()
        waitForDetailsToLoad()

        // Click edit
        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load with current values
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Save Bottle")).fetchSemanticsNodes().isNotEmpty()
        }

        // Update name
        composeTestRule.onNodeWithText("Name *").performTextClearance()
        composeTestRule.onNodeWithText("Name *").performTextInput(updatedName)

        // Scroll to save button and click
        composeTestRule.onNodeWithText("Save Bottle").performScrollTo().performClick()

        // After save, we go back to Details screen (not Gallery)
        // Wait for details screen with updated name
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasContentDescription("Edit")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify updated name appears on details screen
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(updatedName)).fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate back to gallery to verify it's there too
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify updated name in gallery
        composeTestRule.onNodeWithText(updatedName).assertIsDisplayed()
    }

    @Test
    fun viewBottle_backReturnsToGallery() {
        val name = createTestBottle()

        composeTestRule.onNodeWithText(name).performClick()
        composeTestRule.waitForIdle()

        // Wait for details to load
        waitForDetailsToLoad()

        // Verify on details - name appears twice, so check exists
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(name)).fetchSemanticsNodes().isNotEmpty()
        }

        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify back in gallery
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
    }

    @Test
    fun editBottle_backReturnsWithoutSaving() {
        val name = createTestBottle()

        // Navigate to details then edit
        composeTestRule.onNodeWithText(name).performClick()
        waitForDetailsToLoad()
        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Save Bottle")).fetchSemanticsNodes().isNotEmpty()
        }

        // Make a change
        composeTestRule.onNodeWithText("Distillery").performTextClearance()
        composeTestRule.onNodeWithText("Distillery").performTextInput("Changed Distillery")

        // Go back without saving - this goes back to Details screen
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Wait for Details screen (not Gallery)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Edit")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify we're on details with original distillery (changes weren't saved)
        composeTestRule.onNodeWithText("Original Distillery").assertIsDisplayed()

        // Navigate back to gallery
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Should be back in gallery, original name still there
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }
}

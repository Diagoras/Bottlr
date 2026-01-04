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
 * UI tests for the deletion flow using Compose.
 * Tests deleting bottles and cocktails from the details screen.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeleteFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToBottleGallery() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()
    }

    private fun navigateToCocktailGallery() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_Cocktails").performClick()
        composeTestRule.waitForIdle()
    }

    private fun createTestBottle(): String {
        val bottleName = "DeleteTest${System.currentTimeMillis()}"

        navigateToBottleGallery()

        composeTestRule.onNodeWithContentDescription("Add Bottle").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Name *")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Name *").performTextInput(bottleName)
        composeTestRule.onNodeWithText("Distillery").performTextInput("Test Distillery")

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

    private fun createTestCocktail(): String {
        val cocktailName = "DeleteCocktail${System.currentTimeMillis()}"

        navigateToCocktailGallery()

        composeTestRule.onNodeWithContentDescription("Add Cocktail").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Cocktail Name *")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Cocktail Name *").performTextInput(cocktailName)
        composeTestRule.onNodeWithText("Base Spirit (e.g., Vodka, Rum)").performTextInput("Vodka")

        // Scroll to save button and click
        composeTestRule.onNodeWithText("Save Cocktail").performScrollTo().performClick()

        // Wait for navigation back to gallery (longer timeout for emulator)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasText("Search cocktails...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Wait for cocktail to appear in list
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(cocktailName)).fetchSemanticsNodes().isNotEmpty()
        }

        return cocktailName
    }

    private fun waitForDetailsToLoad() {
        // Wait for the Edit button to appear, which indicates data has loaded
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Edit")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun deleteBottle_showsConfirmationDialog() {
        val bottleName = createTestBottle()

        // Navigate to details
        composeTestRule.onNodeWithText(bottleName).performClick()
        waitForDetailsToLoad()

        // Click delete button (it's in the details screen for bottles)
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForIdle()

        // Confirmation dialog should appear
        composeTestRule.onNodeWithText("Delete Bottle").assertIsDisplayed()

        // Cancel to not actually delete
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()

        // Should still be on details screen - check for Edit button
        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
    }

    @Test
    fun deleteBottle_removesFromGallery() {
        val bottleName = createTestBottle()

        // Navigate to details
        composeTestRule.onNodeWithText(bottleName).performClick()
        waitForDetailsToLoad()

        // Click delete button (it's in the details screen for bottles)
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForIdle()

        // Confirm deletion
        composeTestRule.onNodeWithText("Delete").performClick()

        // Should navigate back to gallery
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Bottle should no longer exist in gallery
        composeTestRule.onAllNodes(hasText(bottleName)).fetchSemanticsNodes().isEmpty()
    }

    @Test
    fun deleteCocktail_showsConfirmationDialog() {
        val cocktailName = createTestCocktail()

        // Navigate to details
        composeTestRule.onNodeWithText(cocktailName).performClick()
        waitForDetailsToLoad()

        // Navigate to editor (delete is in editor, not details)
        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Delete")).fetchSemanticsNodes().isNotEmpty()
        }

        // Click delete button
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForIdle()

        // Confirmation dialog should appear
        composeTestRule.onNodeWithText("Delete Cocktail").assertIsDisplayed()

        // Cancel
        composeTestRule.onNodeWithText("Cancel").performClick()
    }

    @Test
    fun deleteCocktail_removesFromGallery() {
        val cocktailName = createTestCocktail()

        // Navigate to details
        composeTestRule.onNodeWithText(cocktailName).performClick()
        waitForDetailsToLoad()

        // Navigate to editor (delete is in editor, not details)
        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Delete")).fetchSemanticsNodes().isNotEmpty()
        }

        // Click delete button
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForIdle()

        // Confirm deletion
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()

        // After deletion from Editor, we land on Details screen (which shows loading/empty state)
        // Wait briefly then navigate back to Gallery
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Back")).fetchSemanticsNodes().isNotEmpty()
        }

        // Click Back to navigate to Gallery
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Should now be on gallery
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search cocktails...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Cocktail should no longer exist
        composeTestRule.onAllNodes(hasText(cocktailName)).fetchSemanticsNodes().isEmpty()
    }

    @Test
    fun deleteButton_notShownOnNewBottleEditor() {
        navigateToBottleGallery()

        // Click FAB to add new bottle
        composeTestRule.onNodeWithContentDescription("Add Bottle").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on editor
        composeTestRule.onNodeWithText("New Bottle").assertIsDisplayed()

        // Delete button should not exist for new items (no Delete icon in top bar)
        composeTestRule.onAllNodes(hasText("Delete Bottle")).fetchSemanticsNodes().isEmpty()
    }

    @Test
    fun deleteButton_notShownOnNewCocktailEditor() {
        navigateToCocktailGallery()

        // Click FAB to add new cocktail
        composeTestRule.onNodeWithContentDescription("Add Cocktail").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on editor
        composeTestRule.onNodeWithText("New Cocktail").assertIsDisplayed()

        // Delete button should not exist for new items
        composeTestRule.onAllNodes(hasText("Delete Cocktail")).fetchSemanticsNodes().isEmpty()
    }
}

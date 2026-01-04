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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the photo picker flow using Compose.
 *
 * Note: The Compose implementation uses PickVisualMedia which opens
 * the system photo picker. We test that the UI elements are present
 * and clickable. Actual photo picker interactions require instrumented
 * testing with mocked ActivityResultLauncher.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PhotoFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToBottleEditor() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_LiquorCabinet").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Add Bottle").performClick()
        composeTestRule.waitForIdle()
    }

    private fun navigateToCocktailEditor() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_Cocktails").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Add Cocktail").performClick()
        composeTestRule.waitForIdle()
    }

    // === BOTTLE EDITOR PHOTO TESTS ===

    @Test
    fun bottleEditor_photoAreaIsDisplayed() {
        navigateToBottleEditor()

        // Photo area should show "Tap to add photo" placeholder
        composeTestRule.onNodeWithText("Tap to add photo").assertIsDisplayed()
    }

    @Test
    fun bottleEditor_addPhotoIconIsDisplayed() {
        navigateToBottleEditor()

        // Add photo icon should be visible
        composeTestRule.onNodeWithContentDescription("Add photo").assertIsDisplayed()
    }

    @Test
    fun bottleEditor_formFieldsAreDisplayed() {
        navigateToBottleEditor()

        // Verify form fields are visible (don't click photo as it opens system picker)
        composeTestRule.onNodeWithText("Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Distillery").assertIsDisplayed()
    }

    // === COCKTAIL EDITOR PHOTO TESTS ===

    @Test
    fun cocktailEditor_photoAreaIsDisplayed() {
        navigateToCocktailEditor()

        // Photo area should show "Tap to add photo" placeholder
        composeTestRule.onNodeWithText("Tap to add photo").assertIsDisplayed()
    }

    @Test
    fun cocktailEditor_addPhotoIconIsDisplayed() {
        navigateToCocktailEditor()

        // Add photo icon should be visible
        composeTestRule.onNodeWithContentDescription("Add photo").assertIsDisplayed()
    }

    @Test
    fun cocktailEditor_formFieldsAreDisplayed() {
        navigateToCocktailEditor()

        // Verify form fields are visible (don't click photo as it opens system picker)
        composeTestRule.onNodeWithText("Cocktail Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Base Spirit (e.g., Vodka, Rum)").assertIsDisplayed()
    }
}

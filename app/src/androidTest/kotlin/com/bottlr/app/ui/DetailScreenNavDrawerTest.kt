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
 * UI tests for navigation behavior on detail screens using Compose.
 *
 * Tests cover:
 * - Back button navigation from detail screens
 * - Navigation from gallery screens via drawer
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DetailScreenNavDrawerTest {

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
        val bottleName = "NavTest${System.currentTimeMillis()}"

        navigateToBottleGallery()

        composeTestRule.onNodeWithContentDescription("Add Bottle").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Name *")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Name *").performTextInput(bottleName)

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
        val cocktailName = "CocktailNavTest${System.currentTimeMillis()}"

        navigateToCocktailGallery()

        composeTestRule.onNodeWithContentDescription("Add Cocktail").performClick()
        composeTestRule.waitForIdle()

        // Wait for editor to load
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Cocktail Name *")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Cocktail Name *").performTextInput(cocktailName)

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
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasContentDescription("Edit")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun hasContentDescription(desc: String) = androidx.compose.ui.test.hasContentDescription(desc)

    // === BOTTLE DETAILS TESTS ===

    @Test
    fun bottleDetails_backButtonReturnsToGallery() {
        val name = createTestBottle()

        // Navigate to bottle details
        composeTestRule.onNodeWithText(name).performClick()
        composeTestRule.waitForIdle()

        // Wait for details to load
        waitForDetailsToLoad()

        // Verify we're on details - name appears twice, check exists
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(name)).fetchSemanticsNodes().isNotEmpty()
        }

        // Press back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search bottles...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify we're back in gallery
        composeTestRule.onNodeWithText("Search bottles...").assertIsDisplayed()
    }

    @Test
    fun bottleDetails_showsBottleInfo() {
        val name = createTestBottle()

        // Navigate to bottle details
        composeTestRule.onNodeWithText(name).performClick()
        composeTestRule.waitForIdle()

        // Wait for details to load
        waitForDetailsToLoad()

        // Verify details are displayed - name appears twice, check exists
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(name)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // === COCKTAIL DETAILS TESTS ===

    @Test
    fun cocktailDetails_backButtonReturnsToGallery() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        composeTestRule.onNodeWithText(name).performClick()
        composeTestRule.waitForIdle()

        // Wait for details to load
        waitForDetailsToLoad()

        // Verify we're on details - name appears twice, check exists
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(name)).fetchSemanticsNodes().isNotEmpty()
        }

        // Press back
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        // Wait for navigation
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText("Search cocktails...")).fetchSemanticsNodes().isNotEmpty()
        }

        // Verify we're back in gallery
        composeTestRule.onNodeWithText("Search cocktails...").assertIsDisplayed()
    }

    @Test
    fun cocktailDetails_showsCocktailInfo() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        composeTestRule.onNodeWithText(name).performClick()
        composeTestRule.waitForIdle()

        // Wait for details to load
        waitForDetailsToLoad()

        // Verify details are displayed - name appears twice, check exists
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasText(name)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // === GALLERY DRAWER TESTS ===

    @Test
    fun bottleGallery_drawerNavigatesToSettings() {
        navigateToBottleGallery()

        // Open drawer and navigate to settings
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drawer_Settings").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on settings
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
    }

    @Test
    fun cocktailGallery_drawerNavigatesToHome() {
        navigateToCocktailGallery()

        // Open drawer and go home
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drawer_Home").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on home
        composeTestRule.onNodeWithText("Quick Actions").assertIsDisplayed()
    }

    @Test
    fun bottleGallery_drawerNavigatesToCocktails() {
        navigateToBottleGallery()

        // Open drawer and navigate to cocktails
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drawer_Cocktails").performClick()
        composeTestRule.waitForIdle()

        // Verify we're on cocktails
        composeTestRule.onNodeWithText("Cocktail Menu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search cocktails...").assertIsDisplayed()
    }
}

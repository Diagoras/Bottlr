package com.bottlr.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import com.bottlr.app.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the deletion flow.
 * Tests deleting bottles and cocktails from the editor screen.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeleteFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToBottleGallery() {
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_liquorcab_button)).perform(click())
        Thread.sleep(500)
    }

    private fun navigateToCocktailGallery() {
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_cocktail_button)).perform(click())
        Thread.sleep(500)
    }

    private fun createTestBottle(): String {
        val bottleName = "DeleteTest${System.currentTimeMillis()}"

        navigateToBottleGallery()

        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        onView(withId(R.id.bottleNameField)).perform(replaceText(bottleName))
        onView(withId(R.id.distillerField)).perform(replaceText("Test Distillery"))

        closeSoftKeyboard()
        Thread.sleep(300)

        onView(withId(R.id.saveButton)).perform(scrollTo(), click())
        Thread.sleep(3000)

        return bottleName
    }

    private fun createTestCocktail(): String {
        val cocktailName = "DeleteCocktail${System.currentTimeMillis()}"

        navigateToCocktailGallery()

        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        onView(withId(R.id.cocktailNameField)).perform(replaceText(cocktailName))
        onView(withId(R.id.baseField)).perform(replaceText("Vodka"))

        closeSoftKeyboard()
        Thread.sleep(300)

        onView(withId(R.id.saveButtonCocktail)).perform(scrollTo(), click())
        Thread.sleep(3000)

        return cocktailName
    }

    @Test
    fun deleteBottle_showsConfirmationDialog() {
        val bottleName = createTestBottle()

        // Navigate to details
        onView(withText(bottleName)).perform(click())
        Thread.sleep(1000)

        // Click edit to go to editor
        onView(withId(R.id.editButton)).perform(click())
        Thread.sleep(1000)

        // Delete button should be visible in edit mode
        onView(withId(R.id.deleteButton)).check(matches(isDisplayed()))

        // Click delete
        onView(withId(R.id.deleteButton)).perform(scrollTo(), click())
        Thread.sleep(500)

        // Confirmation dialog should appear
        onView(withText("Delete Bottle")).check(matches(isDisplayed()))
        onView(withText("Are you sure you want to delete this bottle? This cannot be undone."))
            .check(matches(isDisplayed()))

        // Cancel to not actually delete
        onView(withText("Cancel")).perform(click())
        Thread.sleep(500)

        // Should still be on editor
        onView(withId(R.id.bottleNameField)).check(matches(isDisplayed()))
    }

    @Test
    fun deleteBottle_removesFromGallery() {
        val bottleName = createTestBottle()

        // Navigate to details
        onView(withText(bottleName)).perform(click())
        Thread.sleep(1000)

        // Click edit to go to editor
        onView(withId(R.id.editButton)).perform(click())
        Thread.sleep(1000)

        // Click delete
        onView(withId(R.id.deleteButton)).perform(scrollTo(), click())
        Thread.sleep(500)

        // Confirm deletion
        onView(withText("Delete")).perform(click())
        Thread.sleep(3000)

        // Should navigate back to gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))

        // Bottle should no longer exist in gallery
        onView(withText(bottleName)).check(doesNotExist())
    }

    @Test
    fun deleteCocktail_showsConfirmationDialog() {
        val cocktailName = createTestCocktail()

        // Navigate to details
        onView(withText(cocktailName)).perform(click())
        Thread.sleep(1000)

        // Click edit to go to editor
        onView(withId(R.id.editButton)).perform(click())
        Thread.sleep(1000)

        // Delete button should be visible in edit mode
        onView(withId(R.id.deleteButton)).check(matches(isDisplayed()))

        // Click delete
        onView(withId(R.id.deleteButton)).perform(scrollTo(), click())
        Thread.sleep(500)

        // Confirmation dialog should appear
        onView(withText("Delete Cocktail")).check(matches(isDisplayed()))

        // Cancel
        onView(withText("Cancel")).perform(click())
    }

    @Test
    fun deleteCocktail_removesFromGallery() {
        val cocktailName = createTestCocktail()

        // Navigate to details
        onView(withText(cocktailName)).perform(click())
        Thread.sleep(1000)

        // Click edit to go to editor
        onView(withId(R.id.editButton)).perform(click())
        Thread.sleep(1000)

        // Click delete
        onView(withId(R.id.deleteButton)).perform(scrollTo(), click())
        Thread.sleep(500)

        // Confirm deletion
        onView(withText("Delete")).perform(click())
        Thread.sleep(3000)

        // Should navigate back to gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))

        // Cocktail should no longer exist
        onView(withText(cocktailName)).check(doesNotExist())
    }

    @Test
    fun deleteButton_hiddenForNewBottle() {
        navigateToBottleGallery()

        // Click FAB to add new bottle
        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        // Delete button should not be visible for new item
        onView(withId(R.id.deleteButton)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun deleteButton_hiddenForNewCocktail() {
        navigateToCocktailGallery()

        // Click FAB to add new cocktail
        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        // Delete button should not be visible for new item
        onView(withId(R.id.deleteButton)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}

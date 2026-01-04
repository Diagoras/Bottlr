package com.bottlr.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.MainActivity
import com.bottlr.app.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for nav drawer behavior on detail screens.
 *
 * Tests cover:
 * - Tapping scrim closes the drawer
 * - Back press closes drawer instead of navigating back
 * - Nav drawer works correctly on both bottle and cocktail detail screens
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DetailScreenNavDrawerTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun navigateToGallery() {
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
        val bottleName = "NavTest${System.currentTimeMillis()}"

        navigateToGallery()

        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        onView(withId(R.id.bottleNameField)).perform(replaceText(bottleName))
        closeSoftKeyboard()
        Thread.sleep(300)

        onView(withId(R.id.saveButton)).perform(scrollTo(), click())
        Thread.sleep(3000)

        return bottleName
    }

    private fun createTestCocktail(): String {
        val cocktailName = "CocktailNavTest${System.currentTimeMillis()}"

        navigateToCocktailGallery()

        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(1000)

        onView(withId(R.id.cocktailNameField)).perform(replaceText(cocktailName))
        closeSoftKeyboard()
        Thread.sleep(300)

        onView(withId(R.id.saveButtonCocktail)).perform(scrollTo(), click())
        Thread.sleep(3000)

        return cocktailName
    }

    // === BOTTLE DETAILS TESTS ===

    @Test
    fun bottleDetails_menuButtonOpensDrawer() {
        val name = createTestBottle()

        // Navigate to bottle details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Verify we're on details
        onView(withId(R.id.tvBottleName)).check(matches(isDisplayed()))

        // Open drawer
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)

        // Verify drawer is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))
    }

    @Test
    fun bottleDetails_scrimTapClosesDrawer() {
        val name = createTestBottle()

        // Navigate to bottle details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Open drawer
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)

        // Verify drawer is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))

        // Tap the scrim
        onView(withId(R.id.nav_scrim)).perform(click())
        Thread.sleep(500) // Wait for animation

        // Verify we're still on details screen (drawer closed = can see details)
        onView(withId(R.id.tvBottleName)).check(matches(isDisplayed()))

        // Re-open drawer to verify it was actually closed
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))
    }

    @Test
    fun bottleDetails_backPressClosesDrawerNotScreen() {
        val name = createTestBottle()

        // Navigate to bottle details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Open drawer
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)

        // Verify drawer is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))

        // Press back
        pressBack()
        Thread.sleep(500)

        // Verify drawer is closed
        onView(withId(R.id.menu_home_button)).check(matches(not(isDisplayed())))

        // Verify we're STILL on details screen (not navigated back)
        onView(withId(R.id.tvBottleName)).check(matches(isDisplayed()))
        onView(withId(R.id.tvBottleName)).check(matches(withText(name)))
    }

    @Test
    fun bottleDetails_backPressNavigatesWhenDrawerClosed() {
        val name = createTestBottle()

        // Navigate to bottle details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Verify we're on details
        onView(withId(R.id.tvBottleName)).check(matches(isDisplayed()))

        // Press back (drawer is closed)
        pressBack()
        Thread.sleep(500)

        // Verify we navigated back to gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
    }

    // === COCKTAIL DETAILS TESTS ===

    @Test
    fun cocktailDetails_menuButtonOpensDrawer() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Verify we're on details
        onView(withId(R.id.cvCocktailName)).check(matches(isDisplayed()))

        // Open drawer
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)

        // Verify drawer is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))
    }

    @Test
    fun cocktailDetails_scrimTapClosesDrawer() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Open drawer
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)

        // Verify drawer is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))

        // Tap the scrim
        onView(withId(R.id.nav_scrim)).perform(click())
        Thread.sleep(500) // Wait for animation

        // Verify we're still on details screen (drawer closed = can see details)
        onView(withId(R.id.cvCocktailName)).check(matches(isDisplayed()))

        // Re-open drawer to verify it was actually closed
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))
    }

    @Test
    fun cocktailDetails_backPressClosesDrawerNotScreen() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Open drawer
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)

        // Verify drawer is visible
        onView(withId(R.id.menu_home_button)).check(matches(isDisplayed()))

        // Press back
        pressBack()
        Thread.sleep(500)

        // Verify drawer is closed
        onView(withId(R.id.menu_home_button)).check(matches(not(isDisplayed())))

        // Verify we're STILL on details screen (not navigated back)
        onView(withId(R.id.cvCocktailName)).check(matches(isDisplayed()))
        onView(withId(R.id.cvCocktailName)).check(matches(withText(name)))
    }

    @Test
    fun cocktailDetails_backPressNavigatesWhenDrawerClosed() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Verify we're on details
        onView(withId(R.id.cvCocktailName)).check(matches(isDisplayed()))

        // Press back (drawer is closed)
        pressBack()
        Thread.sleep(500)

        // Verify we navigated back to gallery
        onView(withId(R.id.liquorRecycler)).check(matches(isDisplayed()))
    }

    // === DRAWER NAVIGATION TESTS ===

    @Test
    fun bottleDetails_canNavigateViaDrawer() {
        val name = createTestBottle()

        // Navigate to bottle details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Open drawer and go to settings
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_settings_button)).perform(click())
        Thread.sleep(500)

        // Verify we're on settings
        onView(withId(R.id.signed_in_user)).check(matches(isDisplayed()))
    }

    @Test
    fun cocktailDetails_canNavigateViaDrawer() {
        val name = createTestCocktail()

        // Navigate to cocktail details
        onView(withText(name)).perform(click())
        Thread.sleep(1000)

        // Open drawer and go home
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_home_button)).perform(click())
        Thread.sleep(500)

        // Verify we're on home
        onView(withId(R.id.Title_text)).check(matches(withText("Bottlr")))
    }
}

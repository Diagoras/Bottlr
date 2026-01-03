package com.bottlr.app.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.bottlr.app.MainActivity
import com.bottlr.app.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the photo selection flow (camera and gallery).
 *
 * Tests cover:
 * - Photo dialog appears with correct options
 * - Gallery picker intent is launched correctly
 * - Camera intent is launched correctly (with permission granted)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PhotoFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // Grant camera permission automatically for tests
    @get:Rule(order = 2)
    val cameraPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @Before
    fun setup() {
        hiltRule.inject()
        Intents.init()

        // Stub all external intents to prevent actually opening camera/gallery
        intending(hasAction(Intent.ACTION_GET_CONTENT))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
    }

    @After
    fun teardown() {
        Intents.release()
    }

    // === BOTTLE EDITOR PHOTO TESTS ===

    private fun navigateToBottleEditor() {
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_liquorcab_button)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(500)
    }

    @Test
    fun bottleEditor_addPhotoButton_isDisplayed() {
        navigateToBottleEditor()

        onView(withId(R.id.addPhotoButton))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottleEditor_addPhotoButton_showsDialog() {
        navigateToBottleEditor()

        onView(withId(R.id.addPhotoButton))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Dialog should be visible with title
        onView(withText("Add Photo")).check(matches(isDisplayed()))
    }

    @Test
    fun bottleEditor_photoDialog_hasCorrectOptions() {
        navigateToBottleEditor()

        onView(withId(R.id.addPhotoButton))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Verify all options are present
        onView(withText("Take Photo")).check(matches(isDisplayed()))
        onView(withText("Choose from Gallery")).check(matches(isDisplayed()))
        onView(withText("Cancel")).check(matches(isDisplayed()))
    }

    @Test
    fun bottleEditor_chooseFromGallery_launchesGalleryIntent() {
        navigateToBottleEditor()

        onView(withId(R.id.addPhotoButton))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Click "Choose from Gallery"
        onView(withText("Choose from Gallery")).perform(click())

        Thread.sleep(300)

        // Verify gallery intent was launched
        intended(allOf(
            hasAction(Intent.ACTION_GET_CONTENT),
            hasType("image/*")
        ))
    }

    @Test
    fun bottleEditor_takePhoto_launchesCameraIntent() {
        navigateToBottleEditor()

        onView(withId(R.id.addPhotoButton))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Click "Take Photo"
        onView(withText("Take Photo")).perform(click())

        Thread.sleep(300)

        // Verify camera intent was launched
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    @Test
    fun bottleEditor_photoDialog_cancelDismissesDialog() {
        navigateToBottleEditor()

        onView(withId(R.id.addPhotoButton))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Verify dialog is shown
        onView(withText("Add Photo")).check(matches(isDisplayed()))

        // Click cancel
        onView(withText("Cancel")).perform(click())

        Thread.sleep(300)

        // Verify we're still on the editor (save button visible)
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()))
    }

    // === COCKTAIL EDITOR PHOTO TESTS ===

    private fun navigateToCocktailEditor() {
        onView(withId(R.id.menu_icon)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.menu_cocktail_button)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.fab)).perform(click())
        Thread.sleep(500)
    }

    @Test
    fun cocktailEditor_addPhotoButton_isDisplayed() {
        navigateToCocktailEditor()

        onView(withId(R.id.addPhotoButtonCocktail))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun cocktailEditor_addPhotoButton_showsDialog() {
        navigateToCocktailEditor()

        onView(withId(R.id.addPhotoButtonCocktail))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Dialog should be visible with title
        onView(withText("Add Photo")).check(matches(isDisplayed()))
    }

    @Test
    fun cocktailEditor_photoDialog_hasCorrectOptions() {
        navigateToCocktailEditor()

        onView(withId(R.id.addPhotoButtonCocktail))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Verify all options are present
        onView(withText("Take Photo")).check(matches(isDisplayed()))
        onView(withText("Choose from Gallery")).check(matches(isDisplayed()))
        onView(withText("Cancel")).check(matches(isDisplayed()))
    }

    @Test
    fun cocktailEditor_chooseFromGallery_launchesGalleryIntent() {
        navigateToCocktailEditor()

        onView(withId(R.id.addPhotoButtonCocktail))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Click "Choose from Gallery"
        onView(withText("Choose from Gallery")).perform(click())

        Thread.sleep(300)

        // Verify gallery intent was launched
        intended(allOf(
            hasAction(Intent.ACTION_GET_CONTENT),
            hasType("image/*")
        ))
    }

    @Test
    fun cocktailEditor_takePhoto_launchesCameraIntent() {
        navigateToCocktailEditor()

        onView(withId(R.id.addPhotoButtonCocktail))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Click "Take Photo"
        onView(withText("Take Photo")).perform(click())

        Thread.sleep(300)

        // Verify camera intent was launched
        intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    @Test
    fun cocktailEditor_photoDialog_cancelDismissesDialog() {
        navigateToCocktailEditor()

        onView(withId(R.id.addPhotoButtonCocktail))
            .perform(scrollTo(), click())

        Thread.sleep(300)

        // Verify dialog is shown
        onView(withText("Add Photo")).check(matches(isDisplayed()))

        // Click cancel
        onView(withText("Cancel")).perform(click())

        Thread.sleep(300)

        // Verify we're still on the editor (save button visible)
        onView(withId(R.id.saveButtonCocktail)).check(matches(isDisplayed()))
    }
}

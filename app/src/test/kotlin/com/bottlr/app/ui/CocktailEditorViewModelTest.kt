package com.bottlr.app.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.data.repository.CocktailRepository
import com.bottlr.app.ui.editor.CocktailEditorViewModel
import com.bottlr.app.ui.editor.SaveStatus
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for CocktailEditorViewModel.
 *
 * Tests cover:
 * - New cocktail creation (insert path)
 * - Existing cocktail editing (update path)
 * - Photo URI handling
 * - Save status state transitions
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CocktailEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: CocktailRepository

    private lateinit var viewModel: CocktailEditorViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createViewModel(cocktailId: Long = -1L): CocktailEditorViewModel {
        val savedState = SavedStateHandle(mapOf("cocktailId" to cocktailId))
        return CocktailEditorViewModel(repository, savedState)
    }

    // === NEW COCKTAIL TESTS ===

    @Test
    fun `new cocktail - isEditMode is false`() {
        viewModel = createViewModel(cocktailId = -1L)

        assertFalse(viewModel.isEditMode)
    }

    @Test
    fun `new cocktail - initial state is Idle`() {
        viewModel = createViewModel()

        assertEquals(SaveStatus.Idle, viewModel.saveStatus.value)
    }

    @Test
    fun `saveCocktail inserts new cocktail and syncs to Firestore`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 42L
        coEvery { repository.syncToFirestore(42L) } just Runs
        viewModel = createViewModel()

        // When
        viewModel.saveCocktail(
            name = "Old Fashioned",
            base = "Bourbon",
            mixer = "Sugar",
            juice = "",
            liqueur = "",
            garnish = "Orange peel",
            extra = "Bitters",
            notes = "Classic cocktail",
            keywords = "classic, bourbon",
            rating = 9.0f
        )

        // Then
        viewModel.saveStatus.test {
            assertEquals(SaveStatus.Success, awaitItem())
        }
        coVerify { repository.insert(match { it.name == "Old Fashioned" }) }
        coVerify { repository.syncToFirestore(42L) }
    }

    @Test
    fun `saveCocktail ends in Success state`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.syncToFirestore(any()) } just Runs
        viewModel = createViewModel()

        // When
        viewModel.saveCocktail(
            name = "Test", base = "", mixer = "", juice = "",
            liqueur = "", garnish = "", extra = "", notes = "",
            keywords = "", rating = null
        )

        // Then
        assertEquals(SaveStatus.Success, viewModel.saveStatus.value)
    }

    @Test
    fun `saveCocktail with error sets Error status`() = runTest {
        // Given
        coEvery { repository.insert(any()) } throws RuntimeException("Database error")
        viewModel = createViewModel()

        // When
        viewModel.saveCocktail(
            name = "Test", base = "", mixer = "", juice = "",
            liqueur = "", garnish = "", extra = "", notes = "",
            keywords = "", rating = null
        )

        // Then
        viewModel.saveStatus.test {
            val status = awaitItem()
            assertTrue(status is SaveStatus.Error)
            assertEquals("Database error", (status as SaveStatus.Error).message)
        }
    }

    // === EDIT MODE TESTS ===

    @Test
    fun `edit mode - isEditMode is true`() = runTest {
        // Given
        every { repository.getCocktailById(42L) } returns flowOf(TestFixtures.cocktail(id = 42L))

        // When
        viewModel = createViewModel(cocktailId = 42L)

        // Then
        assertTrue(viewModel.isEditMode)
    }

    @Test
    fun `edit mode - loads existing cocktail`() = runTest {
        // Given
        val existingCocktail = TestFixtures.cocktail(id = 42L, name = "Existing Cocktail")
        every { repository.getCocktailById(42L) } returns flowOf(existingCocktail)

        // When
        viewModel = createViewModel(cocktailId = 42L)

        // Then
        viewModel.cocktail.test {
            val loaded = awaitItem()
            assertEquals("Existing Cocktail", loaded?.name)
        }
    }

    @Test
    fun `edit mode - updates existing cocktail`() = runTest {
        // Given
        val existingCocktail = TestFixtures.cocktail(id = 42L)
        every { repository.getCocktailById(42L) } returns flowOf(existingCocktail)
        coEvery { repository.update(any()) } just Runs
        coEvery { repository.syncToFirestore(42L) } just Runs

        viewModel = createViewModel(cocktailId = 42L)

        // When
        viewModel.saveCocktail(
            name = "Updated Name", base = "Vodka", mixer = "Tonic",
            juice = "Lime", liqueur = "", garnish = "Lime wedge",
            extra = "", notes = "Updated", keywords = "updated", rating = 8.0f
        )

        // Then
        coVerify { repository.update(match { it.id == 42L && it.name == "Updated Name" }) }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    // === PHOTO URI TESTS ===

    @Test
    fun `setPhotoUri updates photoUri state`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>()

        // When
        viewModel.setPhotoUri(testUri)

        // Then
        assertEquals(testUri, viewModel.photoUri.value)
    }

    @Test
    fun `saveCocktail includes photo URI`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.syncToFirestore(any()) } just Runs
        viewModel = createViewModel()

        val testUri = mockk<Uri>()
        every { testUri.toString() } returns "content://test/cocktail_photo.jpg"
        viewModel.setPhotoUri(testUri)

        // When
        viewModel.saveCocktail(
            name = "With Photo", base = "", mixer = "", juice = "",
            liqueur = "", garnish = "", extra = "", notes = "",
            keywords = "", rating = null
        )

        // Then
        coVerify {
            repository.insert(match { it.photoUri == "content://test/cocktail_photo.jpg" })
        }
    }

    @Test
    fun `edit mode - loads photo URI from existing cocktail`() = runTest {
        // Given - mock Uri.parse for the ViewModel's init block
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>()
        every { Uri.parse("content://existing/photo.jpg") } returns mockUri
        every { mockUri.toString() } returns "content://existing/photo.jpg"

        val existingCocktail = TestFixtures.cocktail(
            id = 42L,
            photoUri = "content://existing/photo.jpg"
        )
        every { repository.getCocktailById(42L) } returns flowOf(existingCocktail)

        // When
        viewModel = createViewModel(cocktailId = 42L)

        // Then
        viewModel.photoUri.test {
            val uri = awaitItem()
            assertEquals("content://existing/photo.jpg", uri?.toString())
        }

        unmockkStatic(Uri::class)
    }

    @Test
    fun `photo URI can be changed in edit mode`() = runTest {
        // Given - mock Uri.parse for the ViewModel's init block
        mockkStatic(Uri::class)
        val oldUri = mockk<Uri>()
        every { Uri.parse("content://old/photo.jpg") } returns oldUri

        val existingCocktail = TestFixtures.cocktail(
            id = 42L,
            photoUri = "content://old/photo.jpg"
        )
        every { repository.getCocktailById(42L) } returns flowOf(existingCocktail)
        coEvery { repository.update(any()) } just Runs
        coEvery { repository.syncToFirestore(any()) } just Runs

        viewModel = createViewModel(cocktailId = 42L)

        // When - change the photo
        val newUri = mockk<Uri>()
        every { newUri.toString() } returns "content://new/photo.jpg"
        viewModel.setPhotoUri(newUri)

        viewModel.saveCocktail(
            name = "Updated", base = "", mixer = "", juice = "",
            liqueur = "", garnish = "", extra = "", notes = "",
            keywords = "", rating = null
        )

        // Then
        coVerify {
            repository.update(match { it.photoUri == "content://new/photo.jpg" })
        }

        unmockkStatic(Uri::class)
    }

    // === SYNC FAILURE TESTS ===

    @Test
    fun `saveCocktail succeeds locally even if sync fails`() = runTest {
        // Given
        coEvery { repository.insert(any()) } returns 1L
        coEvery { repository.syncToFirestore(any()) } throws RuntimeException("Network error")
        viewModel = createViewModel()

        // When
        viewModel.saveCocktail(
            name = "Test", base = "", mixer = "", juice = "",
            liqueur = "", garnish = "", extra = "", notes = "",
            keywords = "", rating = null
        )

        // Then - save should still succeed
        assertEquals(SaveStatus.Success, viewModel.saveStatus.value)
    }
}

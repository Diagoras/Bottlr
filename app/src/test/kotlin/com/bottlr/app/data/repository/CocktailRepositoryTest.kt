package com.bottlr.app.data.repository

import android.content.Context
import app.cash.turbine.test
import com.bottlr.app.data.local.dao.CocktailDao
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
 * Unit tests for CocktailRepository.
 *
 * Tests cover:
 * - CRUD operations delegation to DAO
 * - Firestore sync operations
 * - Photo upload handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CocktailRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var dao: CocktailDao

    @MockK
    private lateinit var firestore: FirebaseFirestore

    @MockK
    private lateinit var storage: FirebaseStorage

    @MockK
    private lateinit var auth: FirebaseAuth

    @MockK
    private lateinit var context: Context

    private lateinit var repository: CocktailRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { dao.getAllCocktails() } returns flowOf(emptyList())
        every { dao.getAllCocktailsNewestFirst() } returns flowOf(emptyList())
        every { dao.getCocktailCount() } returns flowOf(0)
        repository = CocktailRepository(dao, firestore, storage, auth, context)
    }

    // === CRUD TESTS ===

    @Test
    fun `insert delegates to DAO`() = runTest {
        val cocktail = TestFixtures.cocktail()
        coEvery { dao.insert(cocktail) } returns 42L

        val id = repository.insert(cocktail)

        assertEquals(42L, id)
        coVerify { dao.insert(cocktail) }
    }

    @Test
    fun `update delegates to DAO with updated timestamp`() = runTest {
        val cocktail = TestFixtures.cocktail(id = 1L)
        coEvery { dao.update(any()) } just Runs

        repository.update(cocktail)

        coVerify { dao.update(match { it.id == 1L && it.updatedAt >= cocktail.updatedAt }) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        val cocktail = TestFixtures.cocktail(id = 1L)
        coEvery { dao.delete(cocktail) } just Runs

        repository.delete(cocktail)

        coVerify { dao.delete(cocktail) }
    }

    @Test
    fun `delete without firestoreId does not call Firestore`() = runTest {
        val cocktail = TestFixtures.cocktail(id = 1L, firestoreId = null)
        coEvery { dao.delete(cocktail) } just Runs

        repository.delete(cocktail)

        coVerify { dao.delete(cocktail) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `getCocktailById delegates to DAO`() = runTest {
        val cocktail = TestFixtures.cocktail(id = 42L)
        every { dao.getCocktailById(42L) } returns flowOf(cocktail)

        repository.getCocktailById(42L).test {
            assertEquals(cocktail, awaitItem())
            awaitComplete()
        }
    }

    // === SYNC TESTS ===

    @Test
    fun `syncToFirestore does nothing when user not logged in`() = runTest {
        val cocktail = TestFixtures.cocktail(id = 1L)
        every { dao.getCocktailById(1L) } returns flowOf(cocktail)
        every { auth.currentUser } returns null

        repository.syncToFirestore(1L)

        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `syncToFirestore does nothing when cocktail not found`() = runTest {
        every { dao.getCocktailById(999L) } returns flowOf(null)

        repository.syncToFirestore(999L)

        verify(exactly = 0) { auth.currentUser }
    }

    @Test
    fun `allCocktails delegates to DAO`() = runTest {
        val cocktails = TestFixtures.cocktails(5)
        every { dao.getAllCocktails() } returns flowOf(cocktails)
        every { dao.getAllCocktailsNewestFirst() } returns flowOf(cocktails)
        every { dao.getCocktailCount() } returns flowOf(5)
        val testRepository = CocktailRepository(dao, firestore, storage, auth, context)

        testRepository.allCocktails.test {
            assertEquals(5, awaitItem().size)
            awaitComplete()
        }
    }

    @Test
    fun `cocktailCount delegates to DAO`() = runTest {
        every { dao.getAllCocktails() } returns flowOf(emptyList())
        every { dao.getAllCocktailsNewestFirst() } returns flowOf(emptyList())
        every { dao.getCocktailCount() } returns flowOf(42)
        val testRepository = CocktailRepository(dao, firestore, storage, auth, context)

        testRepository.cocktailCount.test {
            assertEquals(42, awaitItem())
            awaitComplete()
        }
    }
}

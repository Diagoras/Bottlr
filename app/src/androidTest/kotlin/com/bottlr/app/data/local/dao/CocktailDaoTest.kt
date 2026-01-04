package com.bottlr.app.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.bottlr.app.data.local.BottlrDatabase
import com.bottlr.app.data.local.entities.CocktailEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for CocktailDao.
 *
 * Uses an in-memory Room database for fast, isolated tests.
 * Must run on a device/emulator because Room requires Android's SQLite.
 *
 * Tests cover:
 * - Insert, update, delete operations
 * - Query operations (get by ID, get all)
 * - Search operations (by name, base, mixer, keywords)
 * - Sync operations (mark synced)
 */
@RunWith(AndroidJUnit4::class)
class CocktailDaoTest {

    private lateinit var database: BottlrDatabase
    private lateinit var dao: CocktailDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BottlrDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.cocktailDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun testCocktail(
        id: Long = 0L,
        name: String = "Test Cocktail",
        base: String = "Whisky",
        mixer: String = "Ginger Ale",
        keywords: String = "refreshing, simple"
    ) = CocktailEntity(
        id = id,
        name = name,
        base = base,
        mixer = mixer,
        juice = "",
        liqueur = "",
        garnish = "Lime wedge",
        extra = "",
        photoUri = null,
        notes = "Test notes",
        keywords = keywords,
        rating = 7.5f
    )

    // === INSERT TESTS ===

    @Test
    fun insert_returnsGeneratedId() = runTest {
        val cocktail = testCocktail()
        val id = dao.insert(cocktail)
        assertTrue(id > 0)
    }

    @Test
    fun insert_replacesOnConflict() = runTest {
        val cocktail1 = testCocktail(name = "Original")
        val id = dao.insert(cocktail1)

        val cocktail2 = testCocktail(id = id, name = "Replaced")
        dao.insert(cocktail2)

        dao.getCocktailById(id).test {
            assertEquals("Replaced", awaitItem()?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === GET TESTS ===

    @Test
    fun getCocktailById_returnsCocktail() = runTest {
        val id = dao.insert(testCocktail(name = "Find Me"))

        dao.getCocktailById(id).test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Find Me", result?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getCocktailById_returnsNullWhenNotFound() = runTest {
        dao.getCocktailById(999L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllCocktails_returnsSortedByName() = runTest {
        dao.insert(testCocktail(name = "Zombie"))
        dao.insert(testCocktail(name = "Amaretto Sour"))
        dao.insert(testCocktail(name = "Mojito"))

        dao.getAllCocktails().test {
            val results = awaitItem()
            assertEquals(3, results.size)
            assertEquals("Amaretto Sour", results[0].name)
            assertEquals("Mojito", results[1].name)
            assertEquals("Zombie", results[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === UPDATE TESTS ===

    @Test
    fun update_modifiesExistingCocktail() = runTest {
        val id = dao.insert(testCocktail(name = "Before"))

        dao.getCocktailById(id).test {
            val cocktail = awaitItem()!!
            dao.update(cocktail.copy(name = "After"))
            cancelAndIgnoreRemainingEvents()
        }

        dao.getCocktailById(id).test {
            assertEquals("After", awaitItem()?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === DELETE TESTS ===

    @Test
    fun delete_removesCocktail() = runTest {
        val id = dao.insert(testCocktail())

        dao.getCocktailById(id).test {
            val cocktail = awaitItem()!!
            dao.delete(cocktail)
            cancelAndIgnoreRemainingEvents()
        }

        dao.getCocktailById(id).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteAll_removesAllCocktails() = runTest {
        dao.insert(testCocktail(name = "Cocktail 1"))
        dao.insert(testCocktail(name = "Cocktail 2"))
        dao.insert(testCocktail(name = "Cocktail 3"))

        dao.deleteAll()

        dao.getAllCocktails().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === SEARCH TESTS ===

    @Test
    fun searchByName_findsPartialMatch() = runTest {
        dao.insert(testCocktail(name = "Old Fashioned"))
        dao.insert(testCocktail(name = "Manhattan"))
        dao.insert(testCocktail(name = "Whisky Sour"))

        dao.searchByName("%fashion%").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Old Fashioned", results[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByName_isCaseInsensitive() = runTest {
        dao.insert(testCocktail(name = "MARGARITA"))

        dao.searchByName("%margarita%").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByBase_findsMatches() = runTest {
        dao.insert(testCocktail(base = "Bourbon"))
        dao.insert(testCocktail(base = "Vodka"))
        dao.insert(testCocktail(base = "Bourbon Whiskey"))

        dao.searchByBase("%bourbon%").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByMixer_findsMatches() = runTest {
        dao.insert(testCocktail(mixer = "Tonic Water"))
        dao.insert(testCocktail(mixer = "Ginger Beer"))
        dao.insert(testCocktail(mixer = "Soda Water"))

        dao.searchByMixer("%water%").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchByKeywords_findsMatches() = runTest {
        dao.insert(testCocktail(keywords = "sweet, fruity, tropical"))
        dao.insert(testCocktail(keywords = "bitter, strong"))
        dao.insert(testCocktail(keywords = "sweet, creamy"))

        dao.searchByKeywords("%sweet%").test {
            val results = awaitItem()
            assertEquals(2, results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === SYNC TESTS ===

    @Test
    fun markSynced_updatesCocktail() = runTest {
        val id = dao.insert(testCocktail())

        dao.markSynced(id, "firestore-456")

        dao.getCocktailById(id).test {
            val cocktail = awaitItem()!!
            assertEquals("firestore-456", cocktail.firestoreId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // === COUNT TESTS ===

    @Test
    fun getCocktailCount_returnsCorrectCount() = runTest {
        dao.insert(testCocktail(name = "Cocktail 1"))
        dao.insert(testCocktail(name = "Cocktail 2"))

        dao.getCocktailCount().test {
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

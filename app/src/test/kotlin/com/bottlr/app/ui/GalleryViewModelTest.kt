package com.bottlr.app.ui

import app.cash.turbine.test
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.data.repository.CocktailRepository
import com.bottlr.app.ui.gallery.GalleryViewModel
import com.bottlr.app.util.MainDispatcherRule
import com.bottlr.app.util.TestFixtures
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for GalleryViewModel.
 *
 * Tests cover:
 * - Initial state (bottles mode)
 * - Mode switching between bottles and cocktails
 * - Loading bottles from repository
 * - Current items based on mode
 * - Search filtering for bottles and cocktails
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var repository: BottleRepository

    @MockK
    private lateinit var cocktailRepository: CocktailRepository

    private lateinit var viewModel: GalleryViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Default stub for cocktails (called in constructor)
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(emptyList())
    }

    // === INITIAL STATE TESTS ===

    @Test
    fun `initial mode is bottles (isDrinkMode = true)`() = runTest {
        // Given
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())

        // When
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // Then
        assertTrue(viewModel.isDrinkMode.value)
    }

    @Test
    fun `initial bottles list is empty`() = runTest {
        // Given
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())

        // When
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // Then
        viewModel.bottles.test {
            assertEquals(emptyList<Any>(), awaitItem())
        }
    }

    // === LOADING TESTS ===

    @Test
    fun `bottles flow emits repository data`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(3)
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)

        // When
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // Then
        viewModel.bottles.test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("Whisky 1", result[0].name)
        }
    }

    // === MODE SWITCHING TESTS ===

    @Test
    fun `setDrinkMode to false switches to cocktails mode`() = runTest {
        // Given
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setDrinkMode(false)

        // Then
        assertFalse(viewModel.isDrinkMode.value)
    }

    @Test
    fun `setDrinkMode to true switches back to bottles mode`() = runTest {
        // Given
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        viewModel = GalleryViewModel(repository, cocktailRepository)
        viewModel.setDrinkMode(false)

        // When
        viewModel.setDrinkMode(true)

        // Then
        assertTrue(viewModel.isDrinkMode.value)
    }

    // === BOTTLES/COCKTAILS ITEMS TESTS ===

    @Test
    fun `bottles returns items in bottle mode`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(2)
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When in bottle mode (default)
        assertTrue(viewModel.isDrinkMode.value)

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(2, items.size)
        }
    }

    @Test
    fun `cocktails returns items in cocktail mode`() = runTest {
        // Given
        val cocktails = TestFixtures.cocktails(3)
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(cocktails)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When switching to cocktail mode
        viewModel.setDrinkMode(false)

        // Then
        assertFalse(viewModel.isDrinkMode.value)
        viewModel.cocktails.test {
            val items = awaitItem()
            assertEquals(3, items.size)
        }
    }

    // === SEARCH TESTS - BOTTLES ===

    @Test
    fun `empty search query returns all bottles`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(5)
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When - empty query (default)
        viewModel.setSearchQuery("")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(5, items.size)
        }
    }

    @Test
    fun `search filters bottles by name`() = runTest {
        // Given
        val bottles = listOf(
            TestFixtures.bottle(id = 1, name = "Lagavulin 16"),
            TestFixtures.bottle(id = 2, name = "Ardbeg 10"),
            TestFixtures.bottle(id = 3, name = "Laphroaig Quarter Cask")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("Ardbeg")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Ardbeg 10", items[0].name)
        }
    }

    @Test
    fun `search filters bottles by distillery`() = runTest {
        // Given
        val bottles = listOf(
            TestFixtures.bottle(id = 1, name = "Whisky A", distillery = "Glenfiddich"),
            TestFixtures.bottle(id = 2, name = "Whisky B", distillery = "Macallan"),
            TestFixtures.bottle(id = 3, name = "Whisky C", distillery = "Glenfiddich")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("Glenfiddich")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(2, items.size)
        }
    }

    @Test
    fun `search filters bottles by type`() = runTest {
        // Given
        val bottles = listOf(
            TestFixtures.bottle(id = 1, name = "Whisky A", type = "Single Malt"),
            TestFixtures.bottle(id = 2, name = "Whisky B", type = "Bourbon"),
            TestFixtures.bottle(id = 3, name = "Whisky C", type = "Single Malt")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("Bourbon")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Whisky B", items[0].name)
        }
    }

    @Test
    fun `search filters bottles by keywords`() = runTest {
        // Given
        val bottles = listOf(
            TestFixtures.bottle(id = 1, name = "Lagavulin", keywords = "peaty, smoky, islay"),
            TestFixtures.bottle(id = 2, name = "Glenlivet", keywords = "fruity, floral, speyside"),
            TestFixtures.bottle(id = 3, name = "Ardbeg", keywords = "peaty, intense")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("peaty")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(2, items.size)
        }
    }

    @Test
    fun `search is case insensitive for bottles`() = runTest {
        // Given
        val bottles = listOf(
            TestFixtures.bottle(id = 1, name = "LAGAVULIN"),
            TestFixtures.bottle(id = 2, name = "ardbeg"),
            TestFixtures.bottle(id = 3, name = "Laphroaig")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When - search with different case
        viewModel.setSearchQuery("lagavulin")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("LAGAVULIN", items[0].name)
        }
    }

    // === SEARCH TESTS - COCKTAILS ===

    @Test
    fun `empty search query returns all cocktails`() = runTest {
        // Given
        val cocktails = TestFixtures.cocktails(5)
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(cocktails)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("")

        // Then
        viewModel.cocktails.test {
            val items = awaitItem()
            assertEquals(5, items.size)
        }
    }

    @Test
    fun `search filters cocktails by name`() = runTest {
        // Given
        val cocktails = listOf(
            TestFixtures.cocktail(id = 1, name = "Old Fashioned"),
            TestFixtures.cocktail(id = 2, name = "Whiskey Sour"),
            TestFixtures.cocktail(id = 3, name = "Manhattan")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(cocktails)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("Manhattan")

        // Then
        viewModel.cocktails.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Manhattan", items[0].name)
        }
    }

    @Test
    fun `search filters cocktails by base spirit`() = runTest {
        // Given
        val cocktails = listOf(
            TestFixtures.cocktail(id = 1, name = "Margarita", base = "Tequila"),
            TestFixtures.cocktail(id = 2, name = "Old Fashioned", base = "Bourbon"),
            TestFixtures.cocktail(id = 3, name = "Paloma", base = "Tequila")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(cocktails)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("Tequila")

        // Then
        viewModel.cocktails.test {
            val items = awaitItem()
            assertEquals(2, items.size)
        }
    }

    @Test
    fun `search filters cocktails by keywords`() = runTest {
        // Given
        val cocktails = listOf(
            TestFixtures.cocktail(id = 1, name = "Mojito", keywords = "refreshing, minty, summer"),
            TestFixtures.cocktail(id = 2, name = "Negroni", keywords = "bitter, aperitif"),
            TestFixtures.cocktail(id = 3, name = "Gin Fizz", keywords = "refreshing, citrus")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(cocktails)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("refreshing")

        // Then
        viewModel.cocktails.test {
            val items = awaitItem()
            assertEquals(2, items.size)
        }
    }

    @Test
    fun `search is case insensitive for cocktails`() = runTest {
        // Given
        val cocktails = listOf(
            TestFixtures.cocktail(id = 1, name = "OLD FASHIONED"),
            TestFixtures.cocktail(id = 2, name = "whiskey sour"),
            TestFixtures.cocktail(id = 3, name = "Manhattan")
        )
        every { repository.allBottlesNewestFirst } returns flowOf(emptyList())
        every { cocktailRepository.allCocktailsNewestFirst } returns flowOf(cocktails)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("old fashioned")

        // Then
        viewModel.cocktails.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("OLD FASHIONED", items[0].name)
        }
    }

    @Test
    fun `search with no matches returns empty list`() = runTest {
        // Given
        val bottles = TestFixtures.bottles(3)
        every { repository.allBottlesNewestFirst } returns flowOf(bottles)
        viewModel = GalleryViewModel(repository, cocktailRepository)

        // When
        viewModel.setSearchQuery("xyz123nonexistent")

        // Then
        viewModel.bottles.test {
            val items = awaitItem()
            assertEquals(0, items.size)
        }
    }
}

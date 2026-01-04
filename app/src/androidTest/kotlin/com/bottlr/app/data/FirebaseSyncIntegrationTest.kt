package com.bottlr.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.dao.CocktailDao
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.data.repository.CocktailRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration tests for Firebase sync functionality.
 *
 * IMPORTANT: These tests require Firebase emulators to be running:
 *   firebase emulators:start --only auth,firestore,storage
 *
 * Tests cover:
 * - Syncing bottles to Firestore
 * - Syncing bottles from Firestore
 * - Syncing cocktails to Firestore
 * - Syncing cocktails from Firestore
 * - Deleting items (local + Firestore)
 * - Data erasure
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FirebaseSyncIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bottleRepository: BottleRepository

    @Inject
    lateinit var cocktailRepository: CocktailRepository

    @Inject
    lateinit var bottleDao: BottleDao

    @Inject
    lateinit var cocktailDao: CocktailDao

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    private var testUserId: String? = null

    @Before
    fun setup() {
        hiltRule.inject()

        // Clean up local database before each test
        runBlocking {
            bottleDao.deleteAll()
            cocktailDao.deleteAll()
        }

        // Create a test user in the emulator
        runBlocking {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(
                    "test${System.currentTimeMillis()}@test.com",
                    "testpassword123"
                ).await()
                testUserId = result.user?.uid
            } catch (e: Exception) {
                // User might already exist, try to sign in
                val result = firebaseAuth.signInWithEmailAndPassword(
                    "test@test.com",
                    "testpassword123"
                ).await()
                testUserId = result.user?.uid
            }
        }

        // Clean up Firestore before each test
        runBlocking {
            testUserId?.let { userId ->
                try {
                    val bottlesSnapshot = firestore.collection("users")
                        .document(userId)
                        .collection("bottles")
                        .get()
                        .await()
                    bottlesSnapshot.documents.forEach { it.reference.delete().await() }

                    val cocktailsSnapshot = firestore.collection("users")
                        .document(userId)
                        .collection("cocktails")
                        .get()
                        .await()
                    cocktailsSnapshot.documents.forEach { it.reference.delete().await() }
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    @After
    fun teardown() {
        // Clean up test data
        runBlocking {
            testUserId?.let { userId ->
                try {
                    // Delete all bottles from Firestore
                    val bottlesSnapshot = firestore.collection("users")
                        .document(userId)
                        .collection("bottles")
                        .get()
                        .await()
                    bottlesSnapshot.documents.forEach { it.reference.delete().await() }

                    // Delete all cocktails from Firestore
                    val cocktailsSnapshot = firestore.collection("users")
                        .document(userId)
                        .collection("cocktails")
                        .get()
                        .await()
                    cocktailsSnapshot.documents.forEach { it.reference.delete().await() }
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
            firebaseAuth.signOut()
        }
    }

    // === BOTTLE SYNC TESTS ===

    @Test
    fun syncBottleToFirestore_uploadsBottleData() = runTest {
        // Given - a bottle in local database
        val bottle = BottleEntity(
            name = "Test Whisky",
            distillery = "Test Distillery",
            type = "Single Malt",
            abv = 46.0f,
            age = 12,
            notes = "Smoky with hints of vanilla",
            region = "Islay",
            keywords = "peaty, smoky",
            rating = 8.5f
        )
        val bottleId = bottleRepository.insert(bottle)

        // When - sync to Firestore
        bottleRepository.syncToFirestore(bottleId)

        // Then - bottle should be in Firestore
        val userId = testUserId!!
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()

        assertTrue("Bottle should be synced to Firestore", snapshot.documents.isNotEmpty())
        val syncedBottle = snapshot.documents.first()
        assertEquals("Test Whisky", syncedBottle.getString("name"))
        assertEquals("Test Distillery", syncedBottle.getString("distillery"))
        assertEquals("Single Malt", syncedBottle.getString("type"))
        assertEquals(46.0, syncedBottle.getDouble("abv")!!, 0.1)
    }

    @Test
    fun syncBottleToFirestore_marksBottleAsSynced() = runTest {
        // Given
        val bottle = BottleEntity(name = "Synced Bottle", distillery = "Test")
        val bottleId = bottleRepository.insert(bottle)

        // When
        bottleRepository.syncToFirestore(bottleId)

        // Then - local bottle should have firestoreId set
        val updatedBottle = bottleRepository.getBottleById(bottleId).first()
        assertNotNull("Bottle should have firestoreId", updatedBottle?.firestoreId)
    }

    @Test
    fun syncAllBottlesToFirestore_uploadsMultipleBottles() = runTest {
        // Given - multiple bottles
        val bottle1 = BottleEntity(name = "Whisky 1", distillery = "Dist 1")
        val bottle2 = BottleEntity(name = "Whisky 2", distillery = "Dist 2")
        val bottle3 = BottleEntity(name = "Whisky 3", distillery = "Dist 3")
        bottleRepository.insert(bottle1)
        bottleRepository.insert(bottle2)
        bottleRepository.insert(bottle3)

        // When
        bottleRepository.syncAllToFirestore()

        // Then
        val userId = testUserId!!
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()

        assertEquals("All 3 bottles should be synced", 3, snapshot.documents.size)
    }

    // === COCKTAIL SYNC TESTS ===

    @Test
    fun syncCocktailToFirestore_uploadsCocktailData() = runTest {
        // Given
        val cocktail = CocktailEntity(
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
        val cocktailId = cocktailRepository.insert(cocktail)

        // When
        cocktailRepository.syncToFirestore(cocktailId)

        // Then
        val userId = testUserId!!
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .get()
            .await()

        assertTrue("Cocktail should be synced", snapshot.documents.isNotEmpty())
        val syncedCocktail = snapshot.documents.first()
        assertEquals("Old Fashioned", syncedCocktail.getString("name"))
        assertEquals("Bourbon", syncedCocktail.getString("base"))
    }

    // === DELETE TESTS ===

    @Test
    fun deleteBottle_removesFromFirestore() = runTest {
        // Given - a synced bottle
        val bottle = BottleEntity(name = "To Delete", distillery = "Test")
        val bottleId = bottleRepository.insert(bottle)
        bottleRepository.syncToFirestore(bottleId)

        // Verify it's in Firestore
        val userId = testUserId!!
        var snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()
        assertEquals("Bottle should be in Firestore", 1, snapshot.documents.size)

        // When - delete the bottle
        val bottleToDelete = bottleRepository.getBottleById(bottleId).first()!!
        bottleRepository.delete(bottleToDelete)

        // Then - should be removed from Firestore
        snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()
        assertEquals("Bottle should be deleted from Firestore", 0, snapshot.documents.size)
    }

    @Test
    fun deleteCocktail_removesFromFirestore() = runTest {
        // Given - a synced cocktail
        val cocktail = CocktailEntity(name = "To Delete", base = "Vodka")
        val cocktailId = cocktailRepository.insert(cocktail)
        cocktailRepository.syncToFirestore(cocktailId)

        // Verify it's in Firestore
        val userId = testUserId!!
        var snapshot = firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .get()
            .await()
        assertEquals("Cocktail should be in Firestore", 1, snapshot.documents.size)

        // When - delete
        val cocktailToDelete = cocktailRepository.getCocktailById(cocktailId).first()!!
        cocktailRepository.delete(cocktailToDelete)

        // Then
        snapshot = firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .get()
            .await()
        assertEquals("Cocktail should be deleted from Firestore", 0, snapshot.documents.size)
    }

    // === SYNC FROM CLOUD TESTS ===

    @Test
    fun syncBottlesFromFirestore_downloadsBottleData() = runTest {
        // Given - data in Firestore (not in local DB)
        val userId = testUserId!!
        firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .add(mapOf(
                "name" to "Cloud Whisky",
                "distillery" to "Cloud Distillery",
                "type" to "Bourbon",
                "abv" to 45.0,
                "age" to 10,
                "notes" to "From the cloud",
                "region" to "Kentucky",
                "keywords" to "cloud, test",
                "rating" to 7.5,
                "updatedAt" to System.currentTimeMillis()
            ))
            .await()

        // When - sync from Firestore
        bottleRepository.syncFromFirestore()

        // Then - bottle should be in local database
        val bottles = bottleRepository.allBottles.first()
        assertTrue("Should have at least one bottle", bottles.isNotEmpty())
        val cloudBottle = bottles.find { it.name == "Cloud Whisky" }
        assertNotNull("Cloud bottle should be downloaded", cloudBottle)
        assertEquals("Cloud Distillery", cloudBottle?.distillery)
    }

    @Test
    fun syncCocktailsFromFirestore_downloadsCocktailData() = runTest {
        // Given - data in Firestore
        val userId = testUserId!!
        firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .add(mapOf(
                "name" to "Cloud Cocktail",
                "base" to "Gin",
                "mixer" to "Tonic",
                "juice" to "Lime",
                "liqueur" to "",
                "garnish" to "Lime wedge",
                "extra" to "",
                "notes" to "From the cloud",
                "keywords" to "gin, tonic",
                "rating" to 8.0,
                "updatedAt" to System.currentTimeMillis()
            ))
            .await()

        // When
        cocktailRepository.syncFromFirestore()

        // Then
        val cocktails = cocktailRepository.allCocktails.first()
        val cloudCocktail = cocktails.find { it.name == "Cloud Cocktail" }
        assertNotNull("Cloud cocktail should be downloaded", cloudCocktail)
        assertEquals("Gin", cloudCocktail?.base)
    }

    // === NOT LOGGED IN TESTS ===

    @Test
    fun syncToFirestore_whenNotLoggedIn_doesNothing() = runTest {
        // Given - sign out
        firebaseAuth.signOut()
        val bottle = BottleEntity(name = "No Sync", distillery = "Test")
        val bottleId = bottleRepository.insert(bottle)

        // When - try to sync (should not throw)
        bottleRepository.syncToFirestore(bottleId)

        // Then - bottle should not have firestoreId
        val updatedBottle = bottleRepository.getBottleById(bottleId).first()
        assertNull("Should not have firestoreId when not logged in", updatedBottle?.firestoreId)
    }

}

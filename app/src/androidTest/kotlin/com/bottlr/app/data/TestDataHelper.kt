package com.bottlr.app.data

import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.dao.CocktailDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Test helper for cleaning up data in tests.
 * This keeps test-only cleanup logic out of production code.
 */
class TestDataHelper @Inject constructor(
    private val bottleDao: BottleDao,
    private val cocktailDao: CocktailDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    /**
     * Delete all bottles from local database.
     */
    suspend fun deleteAllLocalBottles() {
        bottleDao.deleteAll()
    }

    /**
     * Delete all cocktails from local database.
     */
    suspend fun deleteAllLocalCocktails() {
        cocktailDao.deleteAll()
    }

    /**
     * Delete all local data (bottles and cocktails).
     */
    suspend fun deleteAllLocalData() {
        bottleDao.deleteAll()
        cocktailDao.deleteAll()
    }

    /**
     * Delete all bottles from Firestore for the current user.
     */
    suspend fun deleteAllFirestoreBottles() {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }

    /**
     * Delete all cocktails from Firestore for the current user.
     */
    suspend fun deleteAllFirestoreCocktails() {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("cocktails")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }

    /**
     * Delete all Firestore data for the current user.
     */
    suspend fun deleteAllFirestoreData() {
        deleteAllFirestoreBottles()
        deleteAllFirestoreCocktails()
    }
}

package com.bottlr.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.entities.BottleEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BottleRepository @Inject constructor(
    private val bottleDao: BottleDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    val allBottles: Flow<List<BottleEntity>> = bottleDao.getAllBottles()
    val allBottlesNewestFirst: Flow<List<BottleEntity>> = bottleDao.getAllBottlesNewestFirst()
    val bottleCount: Flow<Int> = bottleDao.getBottleCount()

    fun getBottleById(id: Long): Flow<BottleEntity?> = bottleDao.getBottleById(id)

    fun searchByField(field: String, query: String): Flow<List<BottleEntity>> {
        return when (field) {
            "Name" -> bottleDao.searchByName(query)
            "Distillery" -> bottleDao.searchByDistillery(query)
            "Type" -> bottleDao.searchByType(query)
            "Region" -> bottleDao.searchByRegion(query)
            "Keywords" -> bottleDao.searchByKeywords(query)
            else -> allBottles
        }
    }

    suspend fun insert(bottle: BottleEntity): Long {
        return bottleDao.insert(bottle)
    }

    suspend fun update(bottle: BottleEntity) {
        bottleDao.update(bottle.copy(updatedAt = java.time.Instant.now()))
    }

    suspend fun delete(bottle: BottleEntity) {
        bottleDao.delete(bottle)
        // Delete from Firestore if synced
        bottle.firestoreId?.let { deleteFromFirestore(it) }
    }

    // Firestore sync methods
    suspend fun syncToFirestore(bottleId: Long) {
        val bottle = bottleDao.getBottleById(bottleId).first() ?: return
        val userId = auth.currentUser?.uid ?: return

        // Upload photo first to get URL
        val photoUrl = bottle.photoUri?.let { uploadPhoto(bottleId, it) }

        val data = hashMapOf(
            "name" to bottle.name,
            "distillery" to bottle.distillery,
            "type" to bottle.type,
            "abv" to bottle.abv,
            "age" to bottle.age,
            "notes" to bottle.notes,
            "region" to bottle.region,
            "keywords" to bottle.keywords,
            "rating" to bottle.rating,
            "updatedAt" to bottle.updatedAt,
            "photoUrl" to photoUrl
        )

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .document(bottle.firestoreId ?: UUID.randomUUID().toString())

        docRef.set(data).await()
        bottleDao.markSynced(bottleId, docRef.id)
    }

    suspend fun syncAllToFirestore() {
        val bottles = allBottles.first()
        bottles.forEach { bottle ->
            syncToFirestore(bottle.id)
        }
    }

    suspend fun syncFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            // Download photo if available
            val photoUrl = doc.getString("photoUrl")
            val localPhotoUri = photoUrl?.let { downloadPhoto(doc.id, it) }

            val bottle = BottleEntity(
                name = doc.getString("name") ?: "",
                distillery = doc.getString("distillery") ?: "",
                type = doc.getString("type") ?: "",
                abv = doc.getDouble("abv")?.toFloat(),
                age = doc.getLong("age")?.toInt(),
                photoUri = localPhotoUri,
                notes = doc.getString("notes") ?: "",
                region = doc.getString("region") ?: "",
                keywords = doc.getString("keywords") ?: "",
                rating = doc.getDouble("rating")?.toFloat(),
                updatedAt = doc.getLong("updatedAt")?.let { java.time.Instant.ofEpochMilli(it) } ?: java.time.Instant.now(),
                firestoreId = doc.id
            )
            bottleDao.insert(bottle)
        }
    }

    private suspend fun uploadPhoto(bottleId: Long, photoUri: String): String? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val uri = Uri.parse(photoUri)
            val ref = storage.reference.child("users/$userId/bottles/$bottleId.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun downloadPhoto(docId: String, photoUrl: String): String? {
        return try {
            val ref = storage.getReferenceFromUrl(photoUrl)

            // Create local file
            val photosDir = File(context.filesDir, "photos/bottles")
            photosDir.mkdirs()
            val localFile = File(photosDir, "$docId.jpg")

            ref.getFile(localFile).await()
            Uri.fromFile(localFile).toString()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun deleteFromFirestore(firestoreId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("bottles")
            .document(firestoreId)
            .delete()
            .await()
    }
}

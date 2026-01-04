package com.bottlr.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "cocktails")
data class CocktailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val base: String = "",
    val mixer: String = "",
    val juice: String = "",
    val liqueur: String = "",
    val garnish: String = "",
    val extra: String = "",
    val photoUri: String? = null,
    val notes: String = "",
    val keywords: String = "",
    val rating: Float? = null,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),

    // Firestore document ID (null if never synced)
    val firestoreId: String? = null
)

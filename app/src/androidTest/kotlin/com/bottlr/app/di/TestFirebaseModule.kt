package com.bottlr.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test module that replaces FirebaseModule and connects to local emulators.
 *
 * This module is automatically used for ALL instrumented tests via @TestInstallIn.
 *
 * Emulator ports (from Android emulator, use 10.0.2.2 for localhost):
 * - Auth: 9099
 * - Firestore: 8080
 * - Storage: 9199
 *
 * Start emulators before running Firebase tests:
 *   firebase emulators:start --only auth,firestore,storage
 *
 * Tests that don't use Firebase (e.g., UI tests) will still work -
 * they'll connect to emulators but won't actually make calls.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class]
)
object TestFirebaseModule {

    // Use 10.0.2.2 to reach host machine from Android emulator
    private const val EMULATOR_HOST = "10.0.2.2"

    // Track initialization to avoid calling useEmulator multiple times
    private var authInitialized = false
    private var firestoreInitialized = false
    private var storageInitialized = false

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance().also { auth ->
            if (!authInitialized) {
                try {
                    auth.useEmulator(EMULATOR_HOST, 9099)
                } catch (e: IllegalStateException) {
                    // Already configured, ignore
                }
                authInitialized = true
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().also { db ->
            if (!firestoreInitialized) {
                try {
                    db.useEmulator(EMULATOR_HOST, 8080)
                } catch (e: IllegalStateException) {
                    // Already configured, ignore
                }
                firestoreInitialized = true
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance().also { storage ->
            if (!storageInitialized) {
                try {
                    storage.useEmulator(EMULATOR_HOST, 9199)
                } catch (e: IllegalStateException) {
                    // Already configured, ignore
                }
                storageInitialized = true
            }
        }
    }
}

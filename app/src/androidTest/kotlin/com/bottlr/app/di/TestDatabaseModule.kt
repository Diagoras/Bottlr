package com.bottlr.app.di

import android.content.Context
import androidx.room.Room
import com.bottlr.app.data.local.BottlrDatabase
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.dao.CocktailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test module that replaces DatabaseModule with an in-memory database.
 *
 * Benefits:
 * - Clean state for each test (no leftover data)
 * - Faster operations (no disk I/O)
 * - Test isolation
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BottlrDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            BottlrDatabase::class.java
        )
            .allowMainThreadQueries() // Allow queries on main thread for testing
            .build()
    }

    @Provides
    fun provideBottleDao(database: BottlrDatabase): BottleDao {
        return database.bottleDao()
    }

    @Provides
    fun provideCocktailDao(database: BottlrDatabase): CocktailDao {
        return database.cocktailDao()
    }
}

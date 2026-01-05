package com.bottlr.app.di

import com.bottlr.app.data.remote.BottleDbClient
import com.bottlr.app.data.repository.EnrichmentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBottleDbClient(): BottleDbClient {
        return BottleDbClient()
    }

    @Provides
    @Singleton
    fun provideEnrichmentRepository(
        bottleDbClient: BottleDbClient
    ): EnrichmentRepository {
        return EnrichmentRepository(bottleDbClient)
    }
}

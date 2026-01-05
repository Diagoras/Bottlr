package com.bottlr.app.di

import android.content.Context
import com.bottlr.app.ai.CloudApiRecognitionService
import com.bottlr.app.ai.GeminiNanoRecognitionService
import com.bottlr.app.ai.RecognitionServiceProvider
import com.bottlr.app.data.local.ApiKeyStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for AI recognition services.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideApiKeyStore(
        @ApplicationContext context: Context
    ): ApiKeyStore {
        return ApiKeyStore(context)
    }

    @Provides
    @Singleton
    fun provideGeminiNanoRecognitionService(
        @ApplicationContext context: Context
    ): GeminiNanoRecognitionService {
        return GeminiNanoRecognitionService(context)
    }

    @Provides
    @Singleton
    fun provideCloudApiRecognitionService(
        @ApplicationContext context: Context,
        apiKeyStore: ApiKeyStore
    ): CloudApiRecognitionService {
        return CloudApiRecognitionService(context, apiKeyStore)
    }

    @Provides
    @Singleton
    fun provideRecognitionServiceProvider(
        geminiNanoService: GeminiNanoRecognitionService,
        cloudApiService: CloudApiRecognitionService
    ): RecognitionServiceProvider {
        return RecognitionServiceProvider(geminiNanoService, cloudApiService)
    }
}

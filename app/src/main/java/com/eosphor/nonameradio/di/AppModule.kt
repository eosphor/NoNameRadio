package com.eosphor.nonameradio.di

import android.content.Context
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.repository.PlayStationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRadioDroidApp(@ApplicationContext context: Context): RadioDroidApp {
        return context.applicationContext as RadioDroidApp
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(radioDroidApp: RadioDroidApp): OkHttpClient {
        return radioDroidApp.httpClient
    }

    @Provides
    @Singleton
    fun providePlayStationRepository(httpClient: OkHttpClient): PlayStationRepository {
        return PlayStationRepository(httpClient)
    }
}
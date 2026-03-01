package com.wearable.monitor.di

import android.content.Context
import androidx.room.Room
import com.wearable.monitor.data.local.AppDatabase
import com.wearable.monitor.data.local.dao.BiometricDao
import com.wearable.monitor.data.local.dao.SyncStatusDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "wearable_monitor.db"
        ).build()

    @Provides
    fun provideBiometricDao(database: AppDatabase): BiometricDao =
        database.biometricDao()

    @Provides
    fun provideSyncStatusDao(database: AppDatabase): SyncStatusDao =
        database.syncStatusDao()
}

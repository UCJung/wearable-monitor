package com.wearable.monitor.di

import com.wearable.monitor.data.repository.AuthRepository
import com.wearable.monitor.data.repository.AuthRepositoryImpl
import com.wearable.monitor.data.repository.BiometricRepository
import com.wearable.monitor.data.repository.BiometricRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBiometricRepository(impl: BiometricRepositoryImpl): BiometricRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}

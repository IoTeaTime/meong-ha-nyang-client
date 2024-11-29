package com.example.mhnfe.di.module

import com.example.mhnfe.data.remote.api.DeviceApi
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.repository.DeviceRepository
import com.example.mhnfe.data.repository.DeviceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.data.repository.GroupRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideGroupRepository(groupApi: GroupApi): GroupRepository {
        return GroupRepositoryImpl(groupApi)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(deviceApi: DeviceApi): DeviceRepository {
        return DeviceRepositoryImpl(deviceApi)
    }
}
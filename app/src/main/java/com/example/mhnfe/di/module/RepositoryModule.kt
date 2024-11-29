package com.example.mhnfe.di.module

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.response.AccessToken
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
    fun provideGroupRepository(
        groupApi: GroupApi,
        accessTokenDataStore: DataStore<AccessToken>
    ): GroupRepository {
        return GroupRepositoryImpl(groupApi, accessTokenDataStore)
    }
}
package com.example.mhnfe.di.module

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.api.QRApi
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.data.repository.GroupRepositoryImpl
import com.example.mhnfe.data.repository.QRRepositoryImpl
import com.example.mhnfe.domain.repository.QRRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideGroupRepository(
        groupApi: GroupApi,
        accessTokenDataStore: DataStore<AccessToken>,
        thingId: String
    ): GroupRepository {
        return GroupRepositoryImpl(groupApi, accessTokenDataStore, thingId)
    }
    @Provides
    @Singleton
    fun provideQRRepository(
        qrApi: QRApi,
        accessTokenDataStore: DataStore<AccessToken>,
        cctvResponseDataStore: DataStore<CCTVResponseBody>,
        thingId: String
    ): QRRepository {
        return QRRepositoryImpl(qrApi, accessTokenDataStore, cctvResponseDataStore, thingId)
    }
}
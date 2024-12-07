package com.example.mhnfe.di.module

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.DeviceApi
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.api.ImageApi
import com.example.mhnfe.data.remote.api.QRApi
import com.example.mhnfe.data.remote.api.TokenApi
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.repository.DeviceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.data.repository.GroupRepositoryImpl
import com.example.mhnfe.data.repository.ImageRepositoryImpl
import com.example.mhnfe.data.repository.QRRepositoryImpl
import com.example.mhnfe.data.repository.TokenRepositoryImpl
import com.example.mhnfe.domain.repository.QRRepository
import com.example.mhnfe.data.repository.UserRepositoryImpl
import com.example.mhnfe.domain.repository.DeviceRepository
import com.example.mhnfe.domain.repository.ImageRepository
import com.example.mhnfe.domain.repository.TokenRepository
import com.example.mhnfe.domain.repository.UserRepository
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

    @Provides
    @Singleton
    fun provideUserRepository(userApi: UserApi): UserRepository{
        return UserRepositoryImpl(userApi)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(deviceApi: DeviceApi): DeviceRepository {
        return DeviceRepositoryImpl(deviceApi)
    }

    @Provides
    @Singleton
    fun provideTokenRepository(tokenApi: TokenApi) : TokenRepository {
        return TokenRepositoryImpl(tokenApi)
    }
    @Provides
    @Singleton
    fun provideImageRepository(imageApi: ImageApi, cctvResponseDataStore: DataStore<CCTVResponseBody>,) :ImageRepository {
        return ImageRepositoryImpl(imageApi, cctvResponseDataStore)
    }
}
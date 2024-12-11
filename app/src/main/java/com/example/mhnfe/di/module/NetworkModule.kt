package com.example.mhnfe.di.module

import com.example.mhnfe.data.network.AuthAuthenticator
import com.example.mhnfe.data.network.AuthInterceptor
import com.example.mhnfe.data.remote.api.AuthApi
import com.example.mhnfe.data.remote.api.DeviceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.api.ImageApi
import com.example.mhnfe.data.remote.api.QRApi
import com.example.mhnfe.data.remote.api.TokenApi
import com.example.mhnfe.data.remote.api.UserApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 토큰 갱신용 OkHttpClient
    @Provides
    @Singleton
    @Named("tokenRefresh")
    fun provideTokenRefreshOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build() // 기본 설정으로 생성 (필요 시 다른 설정 추가)
    }

    // 토큰 갱신용 Retrofit
    @Provides
    @Singleton
    @Named("tokenRefresh")
    fun provideTokenRefreshRetrofit(@Named("tokenRefresh") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.meonghanyang.kro.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    // TokenApi는 tokenRefresh Retrofit을 사용
    @Provides
    @Singleton
    fun provideTokenApi(@Named("tokenRefresh") retrofit: Retrofit): TokenApi {
        return retrofit.create(TokenApi::class.java)
    }

    @Provides
    @Singleton
    @Named("default")
    fun provideOkHttpClient(authInterceptor: AuthInterceptor, authAuthenticator: AuthAuthenticator): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @Named("default")
    fun provideRetrofit(@Named("default") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.meonghanyang.kro.kr") // Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(@Named("default") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)  // Retrofit을 통해 UserApi 생성
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("default") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)  // Retrofit을 통해 AuthApi 생성
    }

    @Provides
    @Singleton
    fun provideGroupApi(@Named("default") retrofit: Retrofit): GroupApi {
        return retrofit.create(GroupApi::class.java)  // Retrofit을 통해 GroupApi 생성
    }
    @Provides
    @Singleton
    fun provideQRApi(@Named("default") retrofit: Retrofit): QRApi {
        return retrofit.create(QRApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceApi(@Named("default") retrofit: Retrofit): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImageApi(@Named("default") retrofit: Retrofit): ImageApi {
        return retrofit.create(ImageApi::class.java)
    }
}
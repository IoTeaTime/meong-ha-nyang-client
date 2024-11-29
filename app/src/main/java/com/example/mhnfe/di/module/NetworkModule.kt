package com.example.mhnfe.di.module

import com.example.mhnfe.data.remote.api.ApiService
import com.example.mhnfe.data.remote.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.api.QRApi
import com.example.mhnfe.data.remote.api.UserApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.meonghanyang.kro.kr") // Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)  // Retrofit을 통해 UserApi 생성
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)  // Retrofit을 통해 AuthApi 생성
    }

    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi {
        return retrofit.create(GroupApi::class.java)  // Retrofit을 통해 GroupApi 생성
    }
    @Provides
    @Singleton
    fun provideQRApi(retrofit: Retrofit): QRApi {
        return retrofit.create(QRApi::class.java)
    }

//    @Provides
//    @Singleton
//    fun provideUserApi(apiService: ApiService): UserApi {
//        // ApiService를 주입받아 UserApi 생성
//        return apiService.createApiService(UserApi::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideAuthApi(apiService: ApiService): AuthApi {
//        // ApiService를 주입받아 AuthApi 생성
//        return apiService.createApiService(AuthApi::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideGroupApi(apiService: ApiService): GroupApi {
//        // ApiService를 주입받아 GroupApi 생성
//        return apiService.createApiService(GroupApi::class.java)
//    }
//
//    @Provides
//    @Singleton
//    fun provideApiService(retrofit: Retrofit): ApiService {
//        return retrofit.create(ApiService::class.java)
//    }
}
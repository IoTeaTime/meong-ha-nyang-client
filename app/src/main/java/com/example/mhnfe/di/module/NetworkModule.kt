package com.example.mhnfe.di.module

import com.example.mhnfe.data.remote.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.example.mhnfe.data.remote.api.GroupApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideGroupApi(): GroupApi {
        return ApiService.createApiService(GroupApi::class.java)
    }
}
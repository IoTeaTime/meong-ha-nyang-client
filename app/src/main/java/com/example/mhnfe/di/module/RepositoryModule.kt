package com.example.mhnfe.di.module

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
    fun provideGroupRepository(): GroupRepository {
        return GroupRepositoryImpl()
    }
}
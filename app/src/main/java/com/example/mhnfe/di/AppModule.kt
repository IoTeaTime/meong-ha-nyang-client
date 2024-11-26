//package com.example.mhnfe.di
//
//import android.content.Context
//import com.example.mhnfe.data.token.TokenProvider
//import com.example.mhnfe.data.repository.UserRepository
//import com.example.mhnfe.data.api.ApiService
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object AppModule {
//
//    @Provides
//    @Singleton
//    fun provideTokenProvider(context: Context): TokenProvider {
//        return TokenProvider(context)
//    }
//
//    @Provides
//    @Singleton
//    fun provideUserRepository(context: Context): UserRepository {
//        return UserRepository(context)
//    }
//
//    @Provides
//    @Singleton
//    fun provideApiService(
//        context: Context,
//        tokenProvider: TokenProvider,
//        userRepository: UserRepository
//    ): ApiService {
//        return ApiService(context, tokenProvider, userRepository)
//    }
//}

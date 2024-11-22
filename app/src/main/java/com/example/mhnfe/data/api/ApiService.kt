package com.example.mhnfe.data.api

import android.content.Context
import com.example.mhnfe.data.network.AuthInterceptor
import com.example.mhnfe.data.repository.UserRepository
import com.example.mhnfe.data.token.TokenProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class ApiService @Inject constructor(
    private val context: Context,
    private val tokenProvider: TokenProvider,
    private val userRepository: UserRepository
) {
    private val retrofit: Retrofit

    init {
        // AuthInterceptor를 OkHttpClient에 추가
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider, userRepository))
//            .connectTimeout(30, TimeUnit.SECONDS) // 연결 시간 설정 (선택 사항)
//            .readTimeout(30, TimeUnit.SECONDS)    // 읽기 시간 설정 (선택 사항)
            .build()

        // Retrofit 인스턴스를 OkHttpClient와 함께 초기화
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.meonghanyang.kro.kr") // 기본 API URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // OkHttpClient 추가
            .build()
    }

    // Generic method to fetch API response
    fun <T> createApiService(apiClass: Class<T>): T {
        return retrofit.create(apiClass)
    }
}
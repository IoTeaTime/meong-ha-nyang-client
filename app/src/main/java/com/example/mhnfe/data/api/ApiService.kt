package com.example.mhnfe.data.api

import android.content.Context
import com.example.mhnfe.data.network.AuthInterceptor
import com.example.mhnfe.data.repository.UserRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiService {
    // OkHttpClient에 AuthInterceptor 추가
    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context, UserRepository())) // AuthInterceptor 추가
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.meonghanyang.kro.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient(context)) // OkHttpClient 전달
            .build()
    }
//    private val retrofit: Retrofit = Retrofit.Builder()
//        .baseUrl("https://api.meonghanyang.kro.kr")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()

    // API 서비스 생성
    fun <T> createApiService(context: Context, apiClass: Class<T>): T {
        return getRetrofit(context).create(apiClass)
    }
}
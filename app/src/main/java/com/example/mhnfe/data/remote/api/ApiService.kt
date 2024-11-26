package com.example.mhnfe.data.remote.api

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor(
    private val retrofit: Retrofit
) {
    fun <T> createApiService(apiClass: Class<T>): T {
        return retrofit.create(apiClass)
    }
}




//package com.example.mhnfe.data.remote.api
//
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object ApiService {
//    private val retrofit: Retrofit = Retrofit.Builder()
//        .baseUrl("https://api.meonghanyang.kro.kr")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    // Generic method to fetch API response
//    fun <T> createApiService(apiClass: Class<T>): T {
//        return retrofit.create(apiClass)
//    }
//}
package com.example.mhnfe.data.repository

import com.example.mhnfe.data.api.AuthApi
import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.SignUpRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {
    private val api: AuthApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.meonghanyang.kro.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AuthApi::class.java)
    }

    suspend fun signUp(
        email: String,
        password: String,
        passwordConfirm: String,
        nickname: String
    ): ApiResponse {
        val request = SignUpRequest(email, password, passwordConfirm, nickname)
        return api.signUp(request)
    }
}

package com.example.mhnfe.data.repository

import com.example.mhnfe.data.api.ApiService
import com.example.mhnfe.data.api.AuthApi
import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.SignUpRequest

class AuthRepository {
    private val api: AuthApi

    init {
        api = ApiService.createApiService(AuthApi::class.java)
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

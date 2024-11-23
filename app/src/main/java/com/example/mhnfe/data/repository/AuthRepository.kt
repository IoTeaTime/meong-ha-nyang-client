package com.example.mhnfe.data.repository

import com.example.mhnfe.data.api.ApiService
import com.example.mhnfe.data.api.AuthApi
import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.LoginRequest
import com.example.mhnfe.data.model.LoginResponse
import com.example.mhnfe.data.model.RefreshFcmTokenRequest
import com.example.mhnfe.data.model.SignUpRequest
import com.example.mhnfe.data.model.User

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

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {
        val request = LoginRequest(email, password)
        return api.login(request)
    }

    suspend fun refreshFcmToken(jwtToken: String, fcmToken: String): ApiResponse {
        val request = RefreshFcmTokenRequest(fcmToken)
        return api.refreshFcmToken(jwtToken, request)
    }
}
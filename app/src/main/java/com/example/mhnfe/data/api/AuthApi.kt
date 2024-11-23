package com.example.mhnfe.data.api

import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.EmailRequest
import com.example.mhnfe.data.model.LoginRequest
import com.example.mhnfe.data.model.LoginResponse
import com.example.mhnfe.data.model.RefreshFcmTokenRequest

import com.example.mhnfe.data.model.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/open-api/auth/sign-up")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): ApiResponse

    @POST("/open-api/auth/check-email")
    suspend fun checkEmailDuplicate(
        @Body request: EmailRequest

    @POST("/open-api/auth/sign-in")
    suspend fun  login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("/api/fcm/token")
    suspend fun refreshFcmToken(
        @Header("Authorization") authToken: String,
        @Body request: RefreshFcmTokenRequest
    ): ApiResponse
}
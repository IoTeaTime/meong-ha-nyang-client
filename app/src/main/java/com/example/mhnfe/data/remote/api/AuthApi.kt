package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.EmailRequest
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.request.RefreshFcmTokenRequest
import com.example.mhnfe.data.remote.response.SignUpResponse
import com.example.mhnfe.data.remote.response.LoginResponse
import com.example.mhnfe.data.remote.request.SignUpRequest
import com.example.mhnfe.data.remote.response.CheckEmailResponse
import com.example.mhnfe.data.remote.response.FCMResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/open-api/auth/sign-up")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): SignUpResponse

    @POST("/open-api/auth/check-email")
    suspend fun checkEmailDuplicate(
        @Body request: EmailRequest
    ): CheckEmailResponse

    @POST("/open-api/auth/sign-in")
    suspend fun  login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("/api/fcm/token")
    suspend fun refreshFcmToken(
        @Header("Authorization") authToken: String,
        @Body request: RefreshFcmTokenRequest
    ): FCMResponse
}
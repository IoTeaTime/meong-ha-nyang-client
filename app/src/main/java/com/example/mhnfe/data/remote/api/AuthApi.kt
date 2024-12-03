package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.CheckEmailVerificationRequest
import com.example.mhnfe.data.remote.request.EmailRequest
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.request.RefreshFcmTokenRequest
import com.example.mhnfe.data.remote.request.SendEmailVerificationRequest
import com.example.mhnfe.data.remote.request.SendPasswordRequest
import com.example.mhnfe.data.remote.response.SignUpResponse
import com.example.mhnfe.data.remote.response.LoginResponse
import com.example.mhnfe.data.remote.request.SignUpRequest
import com.example.mhnfe.data.remote.response.CheckEmailResponse
import com.example.mhnfe.data.remote.response.CheckEmailVerificationResponse
import com.example.mhnfe.data.remote.response.FCMResponse
import com.example.mhnfe.data.remote.response.SendEmailVerificationResponse
import com.example.mhnfe.data.remote.response.SendPasswordResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
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

    @PATCH("/open-api/auth/change-password")
    suspend fun sendPassword(
        @Body request: SendPasswordRequest
    ): SendPasswordResponse

    @POST("/open-api/auth/email-verification")
    suspend fun sendEmailVerification(
        @Body request: SendEmailVerificationRequest
    ): SendEmailVerificationResponse

    @POST("/open-api/auth/check-verification")
    suspend fun checkEmailVerification(
        @Body request: CheckEmailVerificationRequest
    ): CheckEmailVerificationResponse
}
package com.example.mhnfe.data.api

import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.EmailRequest
import com.example.mhnfe.data.model.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/open-api/auth/sign-up")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): ApiResponse

    @POST("/open-api/auth/check-email")
    suspend fun checkEmailDuplicate(
        @Body request: EmailRequest
    ): ApiResponse
}
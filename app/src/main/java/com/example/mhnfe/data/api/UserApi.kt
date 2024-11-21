package com.example.mhnfe.data.api

import com.example.mhnfe.data.model.RefreshResponse
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi {
    @POST("/api/member/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): RefreshResponse
}
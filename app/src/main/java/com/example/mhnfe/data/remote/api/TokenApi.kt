package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import retrofit2.http.Header
import retrofit2.http.POST

interface TokenApi{
    @POST("/api/member/refresh-token")
    suspend fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): RefreshAccessTokenResponse
}
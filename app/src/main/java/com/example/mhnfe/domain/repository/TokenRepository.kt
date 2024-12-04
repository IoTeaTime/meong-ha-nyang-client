package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse

interface TokenRepository {
    suspend fun getNewAccessToken(refreshToken: String): RefreshAccessTokenResponse
}
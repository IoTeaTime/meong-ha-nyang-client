package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.TokenApi
import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import com.example.mhnfe.domain.repository.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val tokenApi: TokenApi
) : TokenRepository{
    override suspend fun getNewAccessToken(
        refreshToken: String
    ): RefreshAccessTokenResponse {
        return tokenApi.refreshAccessToken(refreshToken)
    }
}
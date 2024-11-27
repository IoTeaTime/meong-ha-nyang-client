package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import com.example.mhnfe.data.remote.response.RefreshedAccessToken

interface UserRepository {
    suspend fun getNewAccessToken(response: RefreshAccessTokenResponse): RefreshedAccessToken
}
package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.ApiService
import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val api: UserApi = apiService.createApiService(UserApi::class.java)

    fun refreshAccessToken(refreshToken: String): RefreshAccessTokenResponse {
        return api.refreshAccessToken(refreshToken)
    }
}
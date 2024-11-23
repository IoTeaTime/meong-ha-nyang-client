package com.example.mhnfe.data.repository

import com.example.mhnfe.data.api.ApiService
import com.example.mhnfe.data.api.UserApi
import com.example.mhnfe.data.model.RefreshResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val api: UserApi = apiService.createApiService(UserApi::class.java)

    fun refreshAccessToken(
        token: String
    ): RefreshResponse{
        return api.refreshToken(token)
    }
}
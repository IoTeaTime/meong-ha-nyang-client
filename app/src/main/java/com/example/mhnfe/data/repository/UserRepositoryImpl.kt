package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import com.example.mhnfe.data.remote.response.RefreshedAccessToken
import com.example.mhnfe.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository{
    override suspend fun getNewAccessToken(response: RefreshAccessTokenResponse): RefreshedAccessToken {
        return response.body
    }
}
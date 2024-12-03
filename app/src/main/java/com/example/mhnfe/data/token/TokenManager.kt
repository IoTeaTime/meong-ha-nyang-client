package com.example.mhnfe.data.token

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val userApi: UserApi,
    private val userRepository: UserRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val refreshTokenDataStore: DataStore<RefreshToken>
) {
    // 리프레시 토큰 가져오기
    suspend fun getRefreshToken(): String {
        val refreshToken = refreshTokenDataStore.data.firstOrNull()?.refreshToken
        return refreshToken ?: throw IllegalStateException("Refresh token is not available")
    }

    // 엑세스 토큰 저장
    suspend fun saveAccessToken(token: String) {
        accessTokenDataStore.updateData { AccessToken(token) }
    }
}

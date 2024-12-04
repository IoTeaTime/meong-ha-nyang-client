package com.example.mhnfe.data.token

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.domain.repository.TokenRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val refreshTokenDataStore: DataStore<RefreshToken> // 리프레시 토큰 데이터 스토어 추가
) {
    // 리프레시 토큰으로 액세스 토큰 갱신
    private suspend fun getNewAccessToken(refreshToken: String): String {
        val response = tokenRepository.getNewAccessToken(refreshToken)
        if (response.result.code == 200) {
            saveAccessToken(response.body.accessToken)
            return response.body.accessToken
        } else {
            throw Exception("Failed to refresh access token: ${response.result.message}")
        }
    }

    // 액세스 토큰 저장
    private suspend fun saveAccessToken(accessToken: String) {
        accessTokenDataStore.updateData { currentToken ->
            currentToken.copy(accessToken = accessToken)
        }
    }

    // 리프레시 토큰으로 액세스 토큰을 재발급받고, 성공 시 액세스 토큰을 반환
    suspend fun refreshAccessToken(): String {
        val refreshToken = refreshTokenDataStore.data.map { it.refreshToken }.first()
        return getNewAccessToken(refreshToken)
    }
}

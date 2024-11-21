package com.example.mhnfe.utils

import android.content.Context
import android.util.Log
import com.example.mhnfe.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenManager(private val context: Context, private val userRepository: UserRepository) {
    // SharedPreferences 객체 초기화
    private val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    // Access Token 가져오기
    fun getAccessToken(): String? {
        return sharedPreferences.getString("accessToken", null)
    }

    // Refresh Token 가져오기
    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refreshToken", null)
    }

    // Access Token 저장
    fun saveAccessToken(token: String, expirationTime: Long) {
        sharedPreferences.edit().apply {
            putString("accessToken", token)
            putLong("accessTokenExpiration", expirationTime)
            apply()
        }
    }

    // Refresh Token 저장
    fun saveRefreshToken(token: String, expirationTime: Long) {
        sharedPreferences.edit().apply {
            putString("refreshToken", token)
            putLong("refreshTokenExpiration", expirationTime)
            apply()
        }
    }

    // 토큰 삭제
    fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
}

package com.example.mhnfe.utils

import android.content.Context
import android.util.Log

object TokenManager {
    // SharedPreferences 객체 초기화
    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    // Access Token 가져오기
    fun getAccessToken(context: Context): String? {
        val accessToken = getSharedPreferences(context).getString("accessToken", null)
        // 토큰 만료 시간 체크
        if (!accessToken.isNullOrEmpty()) {
            val expirationTime = getSharedPreferences(context).getLong("accessTokenExpiration", 0L)
            val currentTime = System.currentTimeMillis()
            if (currentTime > expirationTime) {
                // 토큰 만료되었으면 null 리턴
                Log.d("TokenManager", "Access token has expired")
                return null
            }
        }
        return accessToken
    }

    // Refresh Token 가져오기
    fun getRefreshToken(context: Context): String? {
        return getSharedPreferences(context).getString("refreshToken", null)
    }

    // Access Token 저장
    fun saveAccessToken(context: Context, token: String, expirationTime: Long) {
        val editor = getSharedPreferences(context).edit()
        editor.putString("accessToken", token)
        editor.putLong("accessTokenExpiration", expirationTime)
        editor.apply()
    }

    // Refresh Token 저장
    fun saveRefreshToken(context: Context, token: String) {
        getSharedPreferences(context).edit().putString("refreshToken", token).apply()
    }

    // Access Token 삭제
    fun clearTokens(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}

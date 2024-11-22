package com.example.mhnfe.data.token

import android.content.Context
import android.content.SharedPreferences

class TokenProvider(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    fun getAccessToken(): String? {
        return sharedPreferences.getString("accessToken", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refreshToken", null)
    }

    fun saveAccessToken(token: String) {
        sharedPreferences.edit().apply {
            putString("accessToken", token)
            apply()
        }
    }

    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().apply {
            putString("refreshToken", token)
            apply()
        }
    }
}
package com.example.mhnfe.data.model

import kotlinx.serialization.Serializable


data class ApiResponse(
    val result: Result,
    val body: User
)

data class Result(
    val code: Int,
    val message: String,
    val description: String
)

data class User(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)

// 로그인
data class LoginResponse(
    val result: Result,
    val body: Jwt
)

data class Jwt(
    val memberId: Int,
    val accessToken: String,
    val refreshToken: String
)

// 리프래시 토큰
data class RefreshResponse(
    val result: Result,
    val body: Refresh
)

data class Refresh(
    val newAccessToken: String
)
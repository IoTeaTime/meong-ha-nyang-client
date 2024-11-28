package com.example.mhnfe.data.remote.response

import kotlinx.serialization.Serializable

data class SignUpResponse(
    val result: Result,
    val body: Any? = null
) {
    val data: Any? = null
}

data class CheckEmailResponse(
    val result: Result,
    val body: Any? = null
) {
    val data: Any? = null
}

data class User(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)

data class LoginResponse(
    val result: Result,
    val body: Jwt
)

data class Jwt(
    val memberId: Int,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class AccessToken(
    val accessToken: String
)
@Serializable
data class RefreshToken(
    val refreshToken: String
)

data class FCMResponse(
    val result: Result,
    val body: Any? = null
) {
    val data: Any? = null
}

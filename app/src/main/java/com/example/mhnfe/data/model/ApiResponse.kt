package com.example.mhnfe.data.model

data class ApiResponse(
    val result: Result,
    val body: Any? = null
) {
    val data: Any? = null
}

data class Result(
    val code: Int,
    val message: String,
    val description: String? = null
)

data class User(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val nickname: String
)



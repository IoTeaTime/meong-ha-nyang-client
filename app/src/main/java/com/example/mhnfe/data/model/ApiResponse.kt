package com.example.mhnfe.data.model

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



package com.example.mhnfe.data.remote.response

data class Result(
    val code: Int,
    val message: String,
    val description: String? = null
)
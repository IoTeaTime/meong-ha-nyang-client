package com.example.mhnfe.data.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class GroupRequest(
    val code: Int,
    val message: String,
    val description: String
)
package com.example.mhnfe.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class ChangeCctvNicknameResponse(
    val result: Result,
    val body: ChangeCctvDto? = null
)

@Serializable
data class ChangeCctvDto(
    val cctvId: Long,
    val cctvNickname: String,
    val thingId: String,
    val kvsChannelName: String
)
package com.example.mhnfe.data.remote.request

data class CctvQRRequest(
    val groupId: Int,
    val thingId: String,
    val kvsChannelName: String
)

data class ViewerQRRequest(
    val groupId: Int,
    val thingId: String
)

data class CctvInfoResponseBody(
    val cctvId: Int,
    val cctvNickname: String,
    val thingId: String,
    val kvsChannelName: String
)

data class CctvSelfInfoResponseBody(
    val groupId: Int,
    val cctvId: Int,
    val cctvNickname: String,
    val thingId: String,
    val kvsChannelName: String
)
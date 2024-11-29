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

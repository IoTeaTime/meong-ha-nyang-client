package com.example.mhnfe.data.remote.response

import kotlinx.serialization.Serializable

data class CctvQRResponse(
    val result: Result,
    val body: CCTVResponseBody
)

data class ViewerQRResponse(
    val result: Result,
    val body: EmptyBody
)
@Serializable
object EmptyBody

@Serializable
data class CCTVResponseBody(
    val cctvId: Int,
    val accessToken: String
)

data class CctvInfoResponse(
    val result: Result,
    val body : CctvInfoResponseBody
)

data class CctvSelfInfoResponse(
    val result: Result,
    val body: CctvSelfInfoResponseBody
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
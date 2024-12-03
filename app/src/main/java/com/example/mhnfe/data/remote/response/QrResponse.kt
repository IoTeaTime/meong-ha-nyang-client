package com.example.mhnfe.data.remote.response

import com.example.mhnfe.data.remote.request.CctvInfoResponseBody
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
    val cctvId: Int
)

data class CctvInfoResponse(
    val result: Result,
    val body : CctvInfoResponseBody
)
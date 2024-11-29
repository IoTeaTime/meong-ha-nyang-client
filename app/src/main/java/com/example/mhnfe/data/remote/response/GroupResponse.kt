package com.example.mhnfe.data.remote.response

import com.example.mhnfe.data.remote.request.Group
import com.example.mhnfe.data.remote.request.GroupInfo
import com.example.mhnfe.data.remote.request.QRResponseBody

data class GroupResponse(
    val result: Result,
    val body: Group
)

data class GroupInfoResponse(
    val result: Result,
    val body: GroupInfo
)

data class QRApiResponse(
    val result: Result,
    val body: QRResponseBody
)



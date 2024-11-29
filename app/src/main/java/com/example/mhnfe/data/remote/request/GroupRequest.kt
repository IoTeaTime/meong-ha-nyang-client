package com.example.mhnfe.data.remote.request


data class CreateGroupRequest(
    val thingId: String
)

data class QRResponseBody(
    val groupId: Long,
    val kvsChannelId: String? = null // CCTV일 때만 사용
)

data class GroupInfo(
    val groupId: Long,
    val groupName: String,
    val createdAt: String,
    val cctv: List<CctvInfo>
)


data class CctvInfo(
    val cctvId: Long,
    val cctvNickname: String,
    val thingId: String,
    val kvsChannelName: String,
)

data class Group(
    val groupId: Int,
    val groupName: String,
    val createdAt: String
)

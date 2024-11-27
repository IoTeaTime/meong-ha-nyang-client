package com.example.mhnfe.data.remote.response

import com.example.mhnfe.data.remote.request.GroupRequest
import kotlinx.serialization.Serializable

data class GroupResponse(
    val result: GroupRequest,
    val body: Group
)

data class Group(
    val groupId: Int,
    val groupName: String,
    val createdAt: String
)

data class CreateGroupRequest(
    val thingId: String
)

data class GroupInfoResponse(
    val result: GroupRequest,
    val body: GroupInfo
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

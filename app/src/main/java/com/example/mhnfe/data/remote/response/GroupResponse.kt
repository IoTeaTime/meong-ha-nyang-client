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


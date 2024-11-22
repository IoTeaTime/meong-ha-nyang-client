package com.example.mhnfe.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val result: GroupResult,
    val body: Group
)
@Serializable
data class GroupResult(
    val code: Int,
    val message: String,
    val description: String
)
@Serializable
data class Group(
    val groupId: Int,
    val groupName: String,
    val createdAt: String
)
@Serializable
data class CreateGroupRequest(
    val thingId: String
)


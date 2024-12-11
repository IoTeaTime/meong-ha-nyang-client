package com.example.mhnfe.data.remote.response

import com.example.mhnfe.data.remote.request.CctvInfo
import com.example.mhnfe.data.remote.request.Group
import com.example.mhnfe.data.remote.request.GroupInfo
import kotlinx.serialization.Serializable

data class GroupResponse(
    val result: Result,
    val body: Group
)

// 그룹 가입 정보 응답
data class GroupMemberResponse(
    val result: Result,
    val body: GroupMember
)

data class GroupMember(
    val groupId: Long,
    val role: String
)

// 그룹 회원 리스트 응답
data class GroupMemberInfoResponse(
    val result: Result,
    val body: GroupMemberInfoList
)

data class GroupMemberInfoList(
    val member: List<GroupMemberInfo>
)

data class GroupMemberInfo(
    val groupMemberId: Long,
    val memberId: Long,
    val nickname: String,
    val thingId: String,
    val role: String
)

// 그룹 cctv 리스트 응답
data class CctvListResponse(
    val result: Result,
    val body: CctvList
)

data class CctvList(
    val cctv: List<CctvInfo>
)

data class Group(
    val groupId: Int,
    val groupName: String,
    val createdAt: String
)

data class GroupInfoResponse(
    val result: Result,
    val body: GroupInfo
)

data class QRApiResponse(
    val result: Result,
    val body: QRResponseBody
)

data class QRResponseBody(
    val groupId: Long,
    val kvsChannelId: String? = null // CCTV일 때만 사용
)

@Serializable
data class GroupId(
    val groupId: Long
)
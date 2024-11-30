package com.example.mhnfe.data.remote.response

import kotlinx.serialization.Serializable

// 리프래시 토큰
data class RefreshAccessTokenResponse(
    val result: Result,
    val body: RefreshedAccessToken
)

@Serializable
data class RefreshedAccessToken(
    val newAccessToken: String
)

data class LogoutResponse(
    val result: Result
)

data class DeleteResponse(
    val result: Result
)

data class ChangePasswordResponse(
    val result: Result
)
data class ChangeNicknameOrGroupNameResponse(
    val result: Result,
    val body: ChangeNicknameOrGroupNameBody? = null
)

@Serializable
data class ChangeNicknameOrGroupNameBody(
    val nickname: String,
    val groupName: String
)


data class ProfileResponse(
    val result: Result,
    val body: ProfileBody?
)

data class ProfileBody(
    val member: MemberInfo,
    val group: GropInfo
)

data class MemberInfo(
    val id: Int,
    val email: String,
    val profileImgUrl: String,
    val nickname: String
)

data class GropInfo(
    val id: Int,
    val groupName: String
)
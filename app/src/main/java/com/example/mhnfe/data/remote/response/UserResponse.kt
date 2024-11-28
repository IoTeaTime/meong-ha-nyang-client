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
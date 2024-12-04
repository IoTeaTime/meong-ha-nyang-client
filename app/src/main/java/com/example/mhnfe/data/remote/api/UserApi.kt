package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.ChangeNicknameOrGroupNameRequest
import com.example.mhnfe.data.remote.request.ChangePasswordRequest
import com.example.mhnfe.data.remote.response.ChangeNicknameOrGroupNameResponse
import com.example.mhnfe.data.remote.response.ChangePasswordResponse
import com.example.mhnfe.data.remote.response.DeleteResponse
import com.example.mhnfe.data.remote.response.RefreshAccessTokenResponse
import com.example.mhnfe.data.remote.response.LogoutResponse
import com.example.mhnfe.data.remote.response.ProfileResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @POST("/api/member/refresh-token")
    fun refreshAccessToken(
        @Header("Authorization") refreshToken: String
    ): RefreshAccessTokenResponse

    @POST("/api/member/sign-out")
    suspend fun logout(
        @Header("Authorization") accessToken: String
    ): LogoutResponse

    @DELETE("/api/member")
    suspend fun deleteMember(
        @Header("Authorization") accessToken: String
    ): DeleteResponse

    @PUT("/api/member/password")
    suspend fun changePassword(
        @Header("Authorization") accessToken: String,
        @Body request: ChangePasswordRequest
    ): ChangePasswordResponse

    @PATCH("/api/member/nickname-groupname")
    suspend fun changeNicknameOrGroupName(
        @Header("Authorization") accessToken: String,
        @Body request: ChangeNicknameOrGroupNameRequest
    ): ChangeNicknameOrGroupNameResponse

    @GET("/api/member/{memberId}")
    suspend fun getMemberDetails(
        @Header("Authorization") accessToken: String,
        @Path("memberId") memberId: Int
    ): ProfileResponse
}
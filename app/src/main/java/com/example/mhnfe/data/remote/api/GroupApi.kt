package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.response.ApiResponse
import com.example.mhnfe.data.remote.response.CctvListResponse
import com.example.mhnfe.data.remote.request.CreateGroupRequest
import com.example.mhnfe.data.remote.response.GroupInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupApi {
    @POST("/api/group")
    suspend fun createGroup(
        @Header("Authorization") authToken: String,
        @Body request: CreateGroupRequest
    ): GroupResponse

    @GET("/api/group")
    suspend fun getGroupMember(
        @Header("Authorization") authToken: String
    ): GroupMemberResponse

    @GET("/api/group/{groupId}/member")
    suspend fun getGroupMemberList(
        @Path("groupId") groupId: Long,
        @Header("Authorization") authToken: String
    ): Response<GroupMemberInfoResponse>

    @DELETE("/api/group/{groupId}/member")
    suspend fun exitGroup(
        @Path("groupId") groupId: Long,
        @Header("Authorization") token: String
    ): Response<ApiResponse>

    @GET("/api/group/info-list")
    suspend fun getGroupInfo(
        @Header("Authorization") authToken: String
    ): GroupInfoResponse

    @GET("/api/group/cctv")
    suspend fun generateCctvQR(
        @Header("Authorization") authToken: String
    ): QRApiResponse

    @GET("/api/group/viewer")
    suspend fun generateViewerQR(
        @Header("Authorization") authToken: String
    ): QRApiResponse

    @GET("/api/cctv/list/{groupId}")
    suspend fun getCctvList(
        @Path("groupId") groupId: Long,
        @Header("Authorization") token: String
    ): Response<CctvListResponse>

    @DELETE("/api/group/{groupId}/member/{groupMemberId}")
    suspend fun deleteGroupMember(
        @Path("groupId") groupId: Long,
        @Path("groupMemberId") groupMemberId: Long,
        @Header("Authorization") token: String
    ): Response<ApiResponse>
}
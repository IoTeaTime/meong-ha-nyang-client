package com.example.mhnfe.data.remote.api


import com.example.mhnfe.data.remote.request.CreateGroupRequest
import com.example.mhnfe.data.remote.response.GroupInfoResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface GroupApi {
    @POST("/api/group")
    suspend fun createGroup(
        @Header("Authorization") authToken: String,
        @Body request: CreateGroupRequest
    ): GroupResponse

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
}
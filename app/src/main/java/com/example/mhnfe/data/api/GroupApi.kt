package com.example.mhnfe.data.api


import com.example.mhnfe.data.model.CreateGroupRequest
import com.example.mhnfe.data.model.GroupResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroupApi {
    @POST("/api/group")
    suspend fun createGroup(
        @Header("Authorization") authToken: String,
        @Body request: CreateGroupRequest
    ): GroupResponse
}
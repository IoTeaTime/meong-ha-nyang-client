package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.request.Group
import com.example.mhnfe.data.remote.response.GroupInfoResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse


interface GroupRepository {
    suspend fun getGroup(response: GroupResponse): Group
    suspend fun getGroupInfo(): Result<GroupInfoResponse>
    suspend fun createGroup(): Result<Group>
    suspend fun generateCctvQR(): QRApiResponse
    suspend fun generateViewerQR(): QRApiResponse
}
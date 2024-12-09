package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.ApiResponse
import com.example.mhnfe.data.remote.response.CctvListResponse
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupMemberInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberResponse
import com.example.mhnfe.data.remote.response.GroupInfoResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import retrofit2.Response

interface GroupRepository {
    suspend fun getGroup(response: GroupResponse): Group
    suspend fun getGroupInfo(): Result<GroupInfoResponse>
    suspend fun createGroup(): Result<Group>
    suspend fun generateCctvQR(): QRApiResponse
    suspend fun generateViewerQR(): QRApiResponse
    suspend fun getGroupMember(authToken: String): Response<GroupMemberResponse>
    suspend fun getGroupMemberList(groupId: Long, authToken: String): Response<GroupMemberInfoResponse>
    suspend fun getCctvList(groupId: Long, token: String): Response<CctvListResponse>
    suspend fun deleteGroupMember(groupId: Long, groupMemberId: Long, token: String): Response<ApiResponse>
    suspend fun exitGroup(groupId: Long, token: String): Response<ApiResponse>
}
package com.example.mhnfe.domain.repository

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupMemberInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

interface GroupRepository {
    suspend fun getGroup(response: GroupResponse): Group

    suspend fun getGroupMember(authToken: String): Response<GroupMemberResponse>
    suspend fun getGroupMemberList(groupId: Long, authToken: String): Response<GroupMemberInfoResponse>
    suspend fun generateCctvQR(response: QRApiResponse): QRApiResponse
    suspend fun generateViewerQR(response: QRApiResponse): QRApiResponse
}
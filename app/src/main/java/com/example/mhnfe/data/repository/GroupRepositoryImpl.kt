package com.example.mhnfe.data.repository

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.ApiResponse
import com.example.mhnfe.data.remote.response.CctvListResponse
import com.example.mhnfe.data.remote.request.CreateGroupRequest
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import com.example.mhnfe.domain.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val thingId: String
) : GroupRepository {
    override suspend fun getGroup(response: GroupResponse): Group {
        return Group(
            groupId = response.body.groupId,
            groupName = response.body.groupName,
            createdAt = response.body.createdAt
        )
    }

    override suspend fun createGroup(): Result<Group> {
        return try {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val request = CreateGroupRequest(thingId = thingId)
            val response = withContext(Dispatchers.IO) {
                groupApi.createGroup(token, request)
            }
            Result.success(getGroup(response))
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getGroupInfo(): Result<GroupInfoResponse> {
        return try {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response = withContext(Dispatchers.IO) {
                groupApi.getGroupInfo(token)
            }
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 그룹 가입 정보 조회
    override suspend fun getGroupMember(authToken: String): Response<GroupMemberResponse> {
        return groupApi.getGroupMember(authToken)
    }

    // 그룹 회원 리스트 조회
    override suspend fun getGroupMemberList(
        groupId: Long,
        authToken: String
    ): Response<GroupMemberInfoResponse> {
        return groupApi.getGroupMemberList(groupId, authToken)
    }

    override suspend fun generateCctvQR(): QRApiResponse {
        val token = accessTokenDataStore.data.map { it.accessToken }.first()
        return groupApi.generateCctvQR(token)
    }

    override suspend fun generateViewerQR(): QRApiResponse {
        val token = accessTokenDataStore.data.map { it.accessToken }.first()
        return groupApi.generateViewerQR(token)
    }

    override suspend fun getCctvList(groupId: Long, token: String): Response<CctvListResponse> {
        return groupApi.getCctvList(groupId, token)
    }

    override suspend fun deleteGroupMember(
        groupId: Long,
        groupMemberId: Long,
        token: String
    ): Response<ApiResponse> {
        return groupApi.deleteGroupMember(groupId, groupMemberId, token)
    }

    override  suspend fun exitGroup(
        groupId: Long,
        token: String
    ): Response<ApiResponse>{
        return groupApi.exitGroup(groupId, token)
    }
}
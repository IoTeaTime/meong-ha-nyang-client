package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupMemberInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberResponse
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import com.example.mhnfe.domain.repository.GroupRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi
) : GroupRepository {
    override suspend fun getGroup(response: GroupResponse): Group {
        return response.body
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

    override suspend fun generateCctvQR(response: QRApiResponse): QRApiResponse {
        return response
    }

    override suspend fun generateViewerQR(response: QRApiResponse): QRApiResponse {
        return response
    }
}
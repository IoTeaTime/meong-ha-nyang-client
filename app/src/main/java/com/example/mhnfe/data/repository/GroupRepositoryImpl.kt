package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.data.remote.response.GroupResponse
import com.example.mhnfe.data.remote.response.QRApiResponse
import com.example.mhnfe.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupApi: GroupApi
) : GroupRepository {
    override suspend fun getGroup(response: GroupResponse): Group {
        return response.body
    }
    override suspend fun generateCctvQR(response: QRApiResponse): QRApiResponse {
        return response
    }

    override suspend fun generateViewerQR(response: QRApiResponse): QRApiResponse {
        return response
    }
}
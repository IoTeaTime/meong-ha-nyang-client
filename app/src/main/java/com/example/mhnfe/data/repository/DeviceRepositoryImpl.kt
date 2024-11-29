package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.DeviceApi
import com.example.mhnfe.data.remote.request.ChangeCctvNicknameRequest
import com.example.mhnfe.data.remote.response.ChangeCctvDto
import com.example.mhnfe.data.remote.response.ChangeCctvNicknameResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceApi: DeviceApi
): DeviceRepository {
    override suspend fun changeCctvName(authToken: String, cctvId: Long, cctvName: String): Response<ChangeCctvNicknameResponse> {
        val request = ChangeCctvNicknameRequest(cctvId,cctvName);
        return deviceApi.ChangeCctvName(authToken,request);
    }
}
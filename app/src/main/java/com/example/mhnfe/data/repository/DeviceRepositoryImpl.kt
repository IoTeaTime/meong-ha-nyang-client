package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.DeviceApi
import com.example.mhnfe.data.remote.request.ChangeCctvNicknameRequest
import com.example.mhnfe.data.remote.response.ChangeCctvDto
import com.example.mhnfe.data.remote.response.ChangeCctvNicknameResponse
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import com.example.mhnfe.domain.repository.DeviceRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceApi: DeviceApi
): DeviceRepository {
    override suspend fun deleteDevice(authToken: String, cctvId: Long): Response<DeleteDeviceResponse> {
        return deviceApi.delete(authToken, cctvId)
    }

    override suspend fun changeCctvName(authToken: String, cctvId: Long, cctvName: String): Response<ChangeCctvNicknameResponse> {
        val request = ChangeCctvNicknameRequest(cctvId,cctvName);
        return deviceApi.ChangeCctvName(authToken,request);
    }
}
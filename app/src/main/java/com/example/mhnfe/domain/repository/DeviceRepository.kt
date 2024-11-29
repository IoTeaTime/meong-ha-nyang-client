package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.ChangeCctvNicknameResponse
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import retrofit2.Response

interface DeviceRepository {
    suspend fun deleteDevice(authToken: String, cctvId: Long): Response<DeleteDeviceResponse>
    suspend fun changeCctvName(authToken: String, cctvId: Long, cctvName: String): Response<ChangeCctvNicknameResponse>

}
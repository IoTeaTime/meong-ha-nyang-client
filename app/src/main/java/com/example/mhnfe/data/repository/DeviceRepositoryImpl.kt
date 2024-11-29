package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.api.DeviceApi
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import com.example.mhnfe.data.remote.response.Result
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
}
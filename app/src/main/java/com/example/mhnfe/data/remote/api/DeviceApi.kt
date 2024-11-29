package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Path

interface DeviceApi {
    @DELETE("/api/cctv/{cctvId}/out")
    suspend fun delete(
        @Header("Authorization") authToken: String,
        @Path("cctvId") cctvId: Long
    ): Response<DeleteDeviceResponse>
}
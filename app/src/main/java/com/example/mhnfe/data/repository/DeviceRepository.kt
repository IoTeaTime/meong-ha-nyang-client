package com.example.mhnfe.data.repository

import com.example.mhnfe.data.remote.response.ChangeCctvNicknameResponse
import com.example.mhnfe.data.remote.response.Result
import retrofit2.Response

interface DeviceRepository {
    suspend fun changeCctvName(authToken: String, cctvId: Long, cctvName: String): Response<ChangeCctvNicknameResponse>
}
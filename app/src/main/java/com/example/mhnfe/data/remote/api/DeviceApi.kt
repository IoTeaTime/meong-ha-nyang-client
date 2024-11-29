package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.ChangeCctvNicknameRequest
import com.example.mhnfe.data.remote.response.ChangeCctvNicknameResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH

interface DeviceApi {
    @PATCH("/api/cctv")
    suspend fun ChangeCctvName(
        @Header("Authorization") authToken: String,
        @Body request: ChangeCctvNicknameRequest
    ): Response<ChangeCctvNicknameResponse>
}
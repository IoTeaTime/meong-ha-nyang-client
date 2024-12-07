package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.CctvQRRequest
import com.example.mhnfe.data.remote.request.ViewerQRRequest
import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.CctvQRResponse
import com.example.mhnfe.data.remote.response.CctvSelfInfoResponse
import com.example.mhnfe.data.remote.response.ViewerQRResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface QRApi {
    @POST("/api/cctv-device")
    suspend fun generateCctvQR(
        //데이터를 서버에 전송하기 위해서 씀
        @Body request: CctvQRRequest
    ): CctvQRResponse

    @POST("/api/group/viewer")
    suspend fun generateViewerQR(
        @Header("Authorization") token: String,
        @Body request: ViewerQRRequest
    ): ViewerQRResponse

    @GET("/api/cctv-device")
    suspend fun cctvIdInfo(
        @Header("Authorization") token: String,
    ): CctvSelfInfoResponse
}
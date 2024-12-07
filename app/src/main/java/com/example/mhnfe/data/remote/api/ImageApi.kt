package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.ImageRequest
import com.example.mhnfe.data.remote.request.ImageSaveRequest
import com.example.mhnfe.data.remote.request.ViewerQRRequest
import com.example.mhnfe.data.remote.response.ImageResponse
import com.example.mhnfe.data.remote.response.saveImageResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ImageApi {
    @POST("api/image-device")
    suspend fun imageDevice(
        @Header("Authorization") token: String,
        @Body request: ImageSaveRequest
    ) : saveImageResponse

    @POST("/api/image-device/presigned-url")
    suspend fun  urlImage(
        @Header("Authorization") token: String,
        @Body request: ImageRequest
    ) : ImageResponse
}
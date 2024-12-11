package com.example.mhnfe.data.remote.api

import com.example.mhnfe.data.remote.request.ImageRequest
import com.example.mhnfe.data.remote.request.ImageSaveRequest
import com.example.mhnfe.data.remote.response.ImageListResponse
import com.example.mhnfe.data.remote.response.ImageResponse
import com.example.mhnfe.data.remote.response.saveImageResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Url

interface ImageApi {
    @PUT
    suspend fun uploadToPresignedUrl(
        @Url presignedUrl: String,
        @Body image: RequestBody
    ): Response<Unit>

    @POST("api/image-device")
    suspend fun imageDevice(
        @Header("Authorization") token: String,
        @Body request: ImageSaveRequest
    ): saveImageResponse

    @POST("/api/image-device/presigned-url")
    suspend fun urlImage(
        @Header("Authorization") token: String,
        @Body request: ImageRequest
    ): ImageResponse

    @GET("api/image")
    suspend fun getImages(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: String,
        @Query("day") day: String
    ): ImageListResponse
}
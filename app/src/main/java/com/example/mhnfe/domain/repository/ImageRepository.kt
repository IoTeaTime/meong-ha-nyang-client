package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.ImageListResponse
import com.example.mhnfe.data.remote.response.ImageResponse
import com.example.mhnfe.data.remote.response.saveImageResponse

interface ImageRepository {
    suspend fun getPresignedUrl(imageName: String): ImageResponse
    suspend fun saveImage(imageName: String, imagePath: String): saveImageResponse
    suspend fun uploadToPresignedUrl(presignedUrl: String, imageData: ByteArray): Boolean
    suspend fun getImages(year: Int, month: String, day: String): ImageListResponse
}
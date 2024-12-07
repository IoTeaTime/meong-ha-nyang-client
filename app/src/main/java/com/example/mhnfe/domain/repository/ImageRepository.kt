package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.ImageResponse
import com.example.mhnfe.data.remote.response.saveImageResponse

interface ImageRepository {
    suspend fun getPresignedUrl(imageName: String): ImageResponse
    suspend fun saveImage(imageName: String, imagePath: String): saveImageResponse
}
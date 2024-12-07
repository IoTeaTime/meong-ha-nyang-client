package com.example.mhnfe.data.remote.response

import com.example.mhnfe.data.model.ImageInfo


data class ImageResponse(
    val result: Result,
    val body: ImageResponseBody
)
data class saveImageResponse(
    val result: Result,
    val body: EmptyBody
)
data class ImageListResponse(
    val result: Result,
    val body: ImageListBody
)

data class ImageResponseBody(
    val presignedUrl: String,
    val imageName: String,
    val imagePath: String
)

data class ImageListBody(
    val images : List<ImageInfo>
)

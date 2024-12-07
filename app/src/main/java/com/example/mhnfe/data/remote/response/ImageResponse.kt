package com.example.mhnfe.data.remote.response



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

data class ImageInfo(
    val imageId: Long,
    val imageName: String,
    val imagePath: String,
    val formattedCreatedAt: String
)
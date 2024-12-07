package com.example.mhnfe.data.remote.response

data class ImageResponse(
    val result: Result,
    val body: ImageResponseBody
)
data class saveImageResponse(
    val result: Result,
    val body: EmptyBody
)

data class ImageResponseBody(
    val presignedUrl: String,
    val imageName: String,
    val imagePath: String
)
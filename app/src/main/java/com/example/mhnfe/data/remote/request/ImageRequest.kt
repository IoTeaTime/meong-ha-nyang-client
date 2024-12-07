package com.example.mhnfe.data.remote.request

data class ImageSaveRequest(
    val imageName: String,
    val imagePath: String
)

data class ImageRequest(
    val imageName: String
)
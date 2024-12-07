package com.example.mhnfe.data.repository

import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.ImageApi
import com.example.mhnfe.data.remote.request.ImageRequest
import com.example.mhnfe.data.remote.request.ImageSaveRequest
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.remote.response.ImageResponse
import com.example.mhnfe.data.remote.response.saveImageResponse
import com.example.mhnfe.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val imageApi: ImageApi,
    private val cctvResponseDataStore: DataStore<CCTVResponseBody>,
): ImageRepository {

    override suspend fun getPresignedUrl(imageName: String): ImageResponse {
        val cctvAccessToken = cctvResponseDataStore.data.map { it.accessToken }.first()
        val request = ImageRequest(imageName = imageName)

        val response = withContext(Dispatchers.IO) {
            imageApi.urlImage(
                token = cctvAccessToken,
                request = request
            )
        }
        return response
    }
    override suspend fun saveImage(imageName: String, imagePath: String): saveImageResponse {
        val cctvAccessToken = cctvResponseDataStore.data.map { it.accessToken }.first()
        val request = ImageSaveRequest(
            imageName = imageName,
            imagePath = imagePath
        )
        val response =  withContext(Dispatchers.IO) {
            imageApi.imageDevice(
                token = cctvAccessToken,
                request = request
            )
        }
        return response
    }

}
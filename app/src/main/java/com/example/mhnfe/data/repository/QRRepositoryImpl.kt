package com.example.mhnfe.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import com.example.mhnfe.data.remote.api.QRApi
import com.example.mhnfe.data.remote.request.CctvQRRequest
import com.example.mhnfe.data.remote.request.ViewerQRRequest
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.CctvQRResponse
import com.example.mhnfe.data.remote.response.CctvSelfInfoResponse
import com.example.mhnfe.data.remote.response.ViewerQRResponse
import com.example.mhnfe.domain.repository.QRRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRRepositoryImpl @Inject constructor(
    private val qrApi: QRApi,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val cctvResponseDataStore: DataStore<CCTVResponseBody>,
    private val thingId: String
) : QRRepository {
    override suspend fun generateCctvQR(
        groupId: Int,
        kvsChannelName: String
    ): CctvQRResponse {
        Log.d("QRRepository", "CCTV QR 생성 요청 - groupId: $groupId, thingId: $thingId, kvsChannelName: $kvsChannelName")

        val request = CctvQRRequest(
            groupId = groupId,
            thingId = thingId,
            kvsChannelName = kvsChannelName
        )

        val response = withContext(Dispatchers.IO) {
            qrApi.generateCctvQR(request)
        }

        // CCTV ID 저장
        cctvResponseDataStore.updateData {
            CCTVResponseBody(response.body.cctvId, response.body.accessToken)
        }

        Log.d("QRRepository", "CCTV QR 응답: $response")
        return response
    }
    override suspend fun generateViewerQR(
        groupId: Int
    ): ViewerQRResponse {
        val token = accessTokenDataStore.data.map { it.accessToken }.first()
        Log.d("QRRepository", "Viewer QR 생성 요청 - groupId: $groupId, thingId: $thingId")

        val request = ViewerQRRequest(
            groupId = groupId,
            thingId = thingId
        )

        val response = withContext(Dispatchers.IO) {
            qrApi.generateViewerQR(token, request)
        }
        Log.d("QRRepository", "Viewer QR 응답: $response")
        return response
    }
    override suspend fun getCctvInfo(): CctvSelfInfoResponse {
        val cctvAccessToken = cctvResponseDataStore.data.map { it.accessToken }.first()
        return withContext(Dispatchers.IO) {
            qrApi.cctvIdInfo(cctvAccessToken)
        }
    }

}
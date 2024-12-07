package com.example.mhnfe.domain.repository

import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.CctvQRResponse
import com.example.mhnfe.data.remote.response.CctvSelfInfoResponse
import com.example.mhnfe.data.remote.response.ViewerQRResponse

interface QRRepository {
    suspend fun generateCctvQR(groupId: Int, kvsChannelName: String): CctvQRResponse
    suspend fun generateViewerQR(groupId: Int): ViewerQRResponse
    suspend fun getCctvInfo(): CctvSelfInfoResponse
}
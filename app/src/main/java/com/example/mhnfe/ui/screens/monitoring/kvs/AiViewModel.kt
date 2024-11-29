package com.example.mhnfe.ui.screens.monitoring.kvs

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.domain.ai.BoundingBoxUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor() : ViewModel() {
    private val tag = "AiViewModel"
    fun processFrame(bitmap: Bitmap?) {
        viewModelScope.launch {
            try {
                bitmap?.let { bmp ->
                    Log.e(tag, "Frame received: ${bmp.width}x${bmp.height}")
                    // Bitmap AI 처리
                }
            } catch (e: Exception) {
                Log.e(tag, "Frame processing error", e)
            }
        }
    }

    fun detectEvent(onResult: (String) -> Unit, onPayloadReady: (String) -> Unit) {
        viewModelScope.launch {
            val aiResult = "dog"
            val coordinatesJson = BoundingBoxUtils.boundingBoxJson()

            val payload = """
        {
            "trackingId": 1,
            "timestamp": ${System.currentTimeMillis() / 1000}, 
            "objectType": "$aiResult", 
            "coordinates": $coordinatesJson
        }
        """.trimIndent()
            // Callback
            onResult(aiResult)
            onPayloadReady(payload)
        }
    }

}
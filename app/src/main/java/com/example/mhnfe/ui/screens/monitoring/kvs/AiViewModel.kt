package com.example.mhnfe.ui.screens.monitoring.kvs

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            // input "detect" or "category"
            val aiResult = "dog" // TODO. Replace with actual AI processing logic
            val payload = """
            {
                "trackingId": 1,
                "timestamp": ${System.currentTimeMillis() / 1000}, 
                "objectType": "$aiResult", 
                "location": { 
                    "x1": 30, 
                    "y1": 0,
                    "x2": 30, 
                    "y2": 700,
                    "x3": 1200,
                    "y3": 700,
                    "x4": 1200,
                    "y4": 0
                }
            }
            """.trimIndent()
            // Callback
            onResult(aiResult)
            onPayloadReady(payload)
        }
    }
}
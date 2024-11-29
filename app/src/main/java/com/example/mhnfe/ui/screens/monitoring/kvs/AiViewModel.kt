package com.example.mhnfe.ui.screens.monitoring.kvs

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.domain.ai.BoundingBoxUtils
import com.example.mhnfe.domain.ai.DetectionManager
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
            try {
                Log.d("DetectEvent", "detectEvent() 시작")

                val trackingId = DetectionManager.getNextTrackingId()
                val objectType = DetectionManager.getObjectType()
                val coordinatesJson = BoundingBoxUtils.boundingBoxJson()

                val payload = """
            {
                "trackingId": $trackingId,
                "timestamp": ${System.currentTimeMillis() / 1000}, 
                "objectType": "$objectType",
                "coordinates": $coordinatesJson
            }
            """.trimIndent()
                // Callback
                Log.d("DetectEvent", "onResult() 호출")
                onResult(objectType)

                Log.d("DetectEvent", "onPayloadReady() 호출")
                onPayloadReady(payload)

                Log.d("DetectEvent", "detectEvent() 완료")
            } catch (e: Exception) {
                Log.e("DetectEvent", "detectEvent() 중 오류 발생: ${e.message}", e)
            }
        }
}
    }
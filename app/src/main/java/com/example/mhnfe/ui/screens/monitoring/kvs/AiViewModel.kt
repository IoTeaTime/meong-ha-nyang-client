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

    fun detectEvent(onResult: (Int, String, String)  -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("DetectEvent", "detectEvent() 시작")

                // 여기에 그냥 AI 넣으셔도 됩니다
                val trackingId = DetectionManager.getNextTrackingId()
                val objectType = DetectionManager.getObjectType()
                val coordinatesJson = BoundingBoxUtils.boundingBoxJson()

                onResult(trackingId, objectType, coordinatesJson)

            } catch (e: Exception) {
                Log.e("DetectEvent", "detectEvent() 중 오류 발생: ${e.message}", e)
            }
        }
    }
}
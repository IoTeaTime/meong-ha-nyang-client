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
    fun processFrame(bitmap: Bitmap?) {
        viewModelScope.launch {
            try {
                bitmap?.let { bmp ->
                    // Test Log
                    Log.e("AiViewModel", "Frame received: ${bmp.width}x${bmp.height}")
                    // Bitmap AI 처리
                }
            } catch (e: Exception) {
                Log.e("AiViewModel", "Frame processing error", e)
            }
        }
    }


}
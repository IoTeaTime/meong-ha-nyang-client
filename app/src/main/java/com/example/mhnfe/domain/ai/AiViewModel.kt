package com.example.mhnfe.domain.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {
    private val tag = "AI"

    // 여기에 이벤트 발행 값 넣으면 됩니다.
    fun simulateAIProcessing(onResult: (String) -> Unit, onPayloadReady: (String) -> Unit) {
        viewModelScope.launch {
            Log.d(tag, "AI 분석 시작...")
            delay(8000) // AI 분석 시뮬레이션 (20초 딜레이)

            // AI 분석 결과
            val aiResult = "detected" // (예: "detected" 또는 "not_detected")

            // 분석 결과를 JSON payload로 생성
            val payload = """
            {
                "trackingId": 123,
                "timestamp": ${System.currentTimeMillis() / 1000}, 
                "objectType": "$aiResult", 
                "location": { 
                    "x1": 30, 
                    "y1": 0,
                    "x2": 800, 
                    "y2": 700,
                    "x3": 900,
                    "y3": 700,
                    "x4": 1200,
                    "y4": 0
                }
            }
        """.trimIndent()

            Log.d(tag, "AI 분석 완료")

            // 결과 및 Payload 콜백
            onResult(aiResult)
            onPayloadReady(payload) // MQTT 발행에 사용할 Payload 전달
        }
    }

}

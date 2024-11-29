package com.example.mhnfe.domain.ai

// 하드코딩된 상태
object DetectionManager {
    private var currentTrackingId = 0
    
    fun getNextTrackingId(): Int {
        currentTrackingId += 1
        return currentTrackingId
    }
    
    fun getObjectType(): String {
        return "dog"
    }
}

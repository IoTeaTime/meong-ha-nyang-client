package com.example.mhnfe.domain.ai

object DetectionManager {
    private var currentTrackingId = 0
    
    fun getNextTrackingId(): Int {
        currentTrackingId += 1
        return currentTrackingId
    }
}

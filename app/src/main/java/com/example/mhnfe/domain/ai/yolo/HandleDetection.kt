package com.example.mhnfe.domain.ai.yolo

import android.util.Log

class HandleDetection {

    private val TAG = "HandleDetection"

    fun handleDetectionResults(boundingBoxes: List<BoundingBox>) {
        boundingBoxes.forEach { box ->
            if (box.clsName == "dog" || box.clsName == "cat" || box.clsName == "person") {
                Log.d(TAG, "Detected ${box.clsName}")
            }
        }
    }
}
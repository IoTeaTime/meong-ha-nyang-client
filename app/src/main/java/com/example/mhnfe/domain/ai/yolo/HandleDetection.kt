package com.example.mhnfe.domain.ai.yolo

import android.util.Log

class HandleDetection {

    private val TAG = "HandleDetection"

    fun handleDetectionResults(boundingBoxes: List<BoundingBox>): List<BoundingBox> {
        val filteredBoxes = boundingBoxes.filter {
            it.objectName == "dog" || it.objectName == "cat" || it.objectName == "person"
        }
        filteredBoxes.forEach { box ->
            Log.d(TAG, "Detected ${box.objectName} with confidence: ${box.cnf}")
        }
        return filteredBoxes
    }
}

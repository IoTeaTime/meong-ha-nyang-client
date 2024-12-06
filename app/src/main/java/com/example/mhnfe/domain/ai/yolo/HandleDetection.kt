package com.example.mhnfe.domain.ai.yolo

import android.util.Log

class HandleDetection {
    fun handleDetectionResults(boundingBoxes: List<BoundingBox>): List<BoundingBox> {
        val filteredBoxes = boundingBoxes.filter {
            it.objectName == "dog" || it.objectName == "cat" || it.objectName == "person"
        }
        return filteredBoxes
    }
}

package com.example.mhnfe.domain.ai.yolo

import android.content.Context
import android.graphics.Bitmap
import android.util.Log

class YoloDetectionManager(private val context: Context) {

    private val handleDetection = HandleDetection()
    private val yoloDetector = YoloDetector(
        context = context,
        modelPath = "model.tflite",
        labelPath = "labels.txt",
        detectorListener = object : YoloDetector.DetectorListener {
            override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                Log.d(TAG, "YOLO Detection complete: ${boundingBoxes.size} objects detected")
                handleDetection.handleDetectionResults(boundingBoxes)
                detectionCallback?.invoke(boundingBoxes, inferenceTime)
            }

            override fun onEmptyDetect() {
                Log.d(TAG, "No objects detected.")
                detectionCallback?.invoke(emptyList(), 0)
            }
        }
    )

    private var detectionCallback: ((List<BoundingBox>, Long) -> Unit)? = null

    fun detect(frame: Bitmap, callback: (List<BoundingBox>, Long) -> Unit) {
        // 콜백을 설정하고 YOLO 감지 호출
        detectionCallback = callback
        yoloDetector.detect(frame)
    }

    fun close() {
        yoloDetector.close()
    }

    companion object {
        private const val TAG = "YoloDetectionManager"
    }
}

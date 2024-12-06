package com.example.mhnfe.domain.ai.yolo

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class YoloDetectionManager(private val context: Context) {
    // YOLO 감지 결과를 저장할 LiveData
    private val _detectionResults = MutableLiveData<List<BoundingBox>>()
    private val handleDetection = HandleDetection()
    val detectionResults: LiveData<List<BoundingBox>> get() = _detectionResults

    private val yoloDetector = YoloDetector(
        context = context,
        modelPath = "model.tflite",
        labelPath = "labels.txt",
        detectorListener = object : YoloDetector.DetectorListener {
            override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                // 감지된 YOLO 결과를 로그로 출력
                Log.d(TAG, "YOLO Detection complete: ${boundingBoxes.size} objects detected")
                handleDetection.handleDetectionResults(boundingBoxes)
                _detectionResults.postValue(boundingBoxes)
            }

            override fun onEmptyDetect() {
                // 감지된 객체가 없을 경우 로그 출력
                Log.d(TAG, "No objects detected.")
                _detectionResults.postValue(emptyList())
            }
        }
    )

    fun detect(frame: Bitmap) {
        // YOLO 감지 호출
        yoloDetector.detect(frame)
    }

    fun restart(isGpu: Boolean) {
        yoloDetector.restart(isGpu)
    }

    fun close() {
        yoloDetector.close()
    }

    companion object {
        private const val TAG = "YoloDetectionManager"
    }
}

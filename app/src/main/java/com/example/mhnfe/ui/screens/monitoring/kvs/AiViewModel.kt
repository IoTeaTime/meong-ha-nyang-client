package com.example.mhnfe.ui.screens.monitoring.kvs

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.domain.ai.yolo.BoundingBox
import com.example.mhnfe.domain.ai.BoundingBoxUtils
import com.example.mhnfe.domain.ai.DetectionManager
import com.example.mhnfe.domain.ai.yolo.YoloDetector
import com.example.mhnfe.domain.ai.opencv.BitmapToMatConverter
import com.example.mhnfe.domain.ai.opencv.MotionDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import org.opencv.core.Rect
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // Context 주입
) : ViewModel() {
    private val tag = "AiViewModel"
    private val motionDetector = MotionDetector()


    // YOLO 감지 결과를 저장할 LiveData
    private val _detectionResults = MutableLiveData<List<BoundingBox>>()
    val detectionResults: LiveData<List<BoundingBox>> get() = _detectionResults

    private val yoloDetector = YoloDetector(
        context = context,
        modelPath = "model.tflite",
        labelPath = "labels.txt",
        detectorListener = object : YoloDetector.DetectorListener {
            override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                // 감지된 YOLO 결과를 로그로 출력
                Log.d(tag, "YOLO Detection complete: ${boundingBoxes.size} objects detected")
                _detectionResults.postValue(boundingBoxes)

                // 각 BoundingBox 정보를 상세 로그로 출력
                boundingBoxes.forEach { box ->
                    Log.d(tag, """
                        Detected Object:
                        Class Name: ${box.clsName}
                        Class ID: ${box.cls}
                        Confidence: ${box.cnf}
                        Coordinates: 
                          Top-Left: (${box.x1}, ${box.y1})
                          Bottom-Right: (${box.x2}, ${box.y2})
                          Center: (${box.cx}, ${box.cy})
                        Size: 
                          Width: ${box.w}, Height: ${box.h}
                    """.trimIndent())
                }
            }

            override fun onEmptyDetect() {
                // 감지된 객체가 없을 경우 로그 출력
                Log.d(tag, "No objects detected.")
                _detectionResults.postValue(emptyList())
            }
        }
    )


    fun processFrame(bitmap: Bitmap?) {
        viewModelScope.launch {
            try {
                bitmap?.let { bmp ->
                    Log.d(tag, "Frame received: ${bmp.width}x${bmp.height}")

                    // Bitmap을 Mat으로 변환
                    val currentFrame: Mat = BitmapToMatConverter.BitToMat(bmp)

                    // OpenCV로 움직임 감지
                    val motionAreas: List<Rect> = motionDetector.detectMotion(currentFrame)

                    // 감지된 움직임 영역을 로그에 출력
                    if (motionAreas.isNotEmpty()) {
                        Log.d(tag, "Motion detected in ${motionAreas.size} area(s)")
                        for (area in motionAreas) {
                            Log.d(tag, "Motion area: ${area.x}, ${area.y}, ${area.width}, ${area.height}")
                        }
                    } else {
                        Log.d(tag, "No motion detected")
                    }

                    // 현재 프레임 객체 해제
                    currentFrame.release()
                }
            } catch (e: Exception) {
                Log.e(tag, "Frame processing error", e)
            }
        }
    }

    fun detectEvent(onResult: (String) -> Unit, onPayloadReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("DetectEvent", "detectEvent() 시작")

                val trackingId = DetectionManager.getNextTrackingId()
                val objectType = DetectionManager.getObjectType()
                val coordinatesJson = BoundingBoxUtils.boundingBoxJson()

                val payload = """
            {
                "trackingId": $trackingId,
                "timestamp": ${System.currentTimeMillis() / 1000}, 
                "objectType": "$objectType",
                "coordinates": $coordinatesJson
            }
            """.trimIndent()
                // Callback
                Log.d("DetectEvent", "onResult() 호출")
                onResult(objectType)

                Log.d("DetectEvent", "onPayloadReady() 호출")
                onPayloadReady(payload)

                Log.d("DetectEvent", "detectEvent() 완료")
            } catch (e: Exception) {
                Log.e("DetectEvent", "detectEvent() 중 오류 발생: ${e.message}", e)
            }
        }
    }
}
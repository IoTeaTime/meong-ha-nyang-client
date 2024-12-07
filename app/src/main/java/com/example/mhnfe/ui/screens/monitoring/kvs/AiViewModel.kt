package com.example.mhnfe.ui.screens.monitoring.kvs

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.domain.ai.BoundingBoxUtils
import com.example.mhnfe.domain.ai.DetectionManager
import com.example.mhnfe.domain.ai.opencv.BitmapToMatConverter
import com.example.mhnfe.domain.ai.opencv.MotionDetector
import com.example.mhnfe.domain.ai.yolo.YoloDetectionManager
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
    private val yoloDetectionManager = YoloDetectionManager(context)
    private var lastEventTime: Long = 0 // 마지막 이벤트 발생 시간 기록
    private val eventDelayMillis = 500L // event data to iot 딜레이 시간

    fun processFrame(
        bitmap: Bitmap?,
        onResult: (Int, String, Float, List<Map<String, Float>>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                bitmap?.let { bmp ->
                    val currentFrame: Mat = BitmapToMatConverter.BitToMat(bmp)
                    val motionAreas: List<Rect> = motionDetector.detectMotion(currentFrame)

                    if (motionAreas.isNotEmpty()) {
                        Log.d(tag, "Motion detected in ${motionAreas.size} area(s)")

                        // Yolo 실행 및 콜백 처리
                        yoloDetectionManager.detect(bmp) { boundingBoxes, _ ->
                            if (boundingBoxes.isNotEmpty()) {
                                if (BoundingBoxUtils.shouldTriggerEvent(lastEventTime, eventDelayMillis)) {
                                    lastEventTime = System.currentTimeMillis()
                                    Log.d("DetectEvent", "detectEvent() 시작")

                                    val trackingId = DetectionManager.getNextTrackingId()

                                    // BoundingBoxUtils를 사용하여 데이터 추출
                                    val (objectType, confidence) = BoundingBoxUtils.getTypeAndConfidence(
                                        boundingBoxes
                                    )
                                    val coordinates = BoundingBoxUtils.getCoordinates(boundingBoxes)

                                    onResult(trackingId, objectType, confidence, coordinates)
                                }
                            }
                        }
                    }

                    // 현재 프레임 객체 해제
                    currentFrame.release()
                }
            } catch (e: Exception) {
                Log.e(tag, "Frame processing error", e)
            }
        }
    }
}

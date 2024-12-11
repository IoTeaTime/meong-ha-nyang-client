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
import com.example.mhnfe.domain.ai.yolo.HandleDetection
import com.example.mhnfe.domain.ai.yolo.YoloDetectionManager
import com.example.mhnfe.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class AiViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val tag = "AiViewModel"
    private val motionDetector = MotionDetector()
    private val yoloDetectionManager = YoloDetectionManager(context)
    private var lastEventTime: Long = 0 // 마지막 이벤트 발생 시간 기록
    private val eventDelayMillis = 60000L // event data to iot 딜레이 시간
    private val handleDetection = HandleDetection()

    fun processFrame(
        bitmap: Bitmap?,
        onResult: (Int, String, Float, List<Map<String, Int>>) -> Unit
    ) {
        viewModelScope.launch label@{
            var currentFrame: Mat? = null
            try {
                bitmap?.let { bmp ->
                    currentFrame = BitmapToMatConverter.BitToMat(bmp)
                    val motionAreas: List<Rect> = motionDetector.detectMotion(currentFrame!!)

                    if (motionAreas.isNotEmpty()) {
                        Log.d(tag, "Motion detected in ${motionAreas.size} area(s)")

                        // Yolo 실행 및 콜백 처리
                        yoloDetectionManager.detect(bmp) { boundingBoxes, _ ->
                            val filteredBoxes =
                                handleDetection.handleDetectionResults(boundingBoxes)
                            if (filteredBoxes.isNotEmpty()) {
                                val (imageName, imageData) = createImage(bmp)

                                if (BoundingBoxUtils.shouldTriggerEvent(
                                        lastEventTime,
                                        eventDelayMillis
                                    )
                                ) {
                                    lastEventTime = System.currentTimeMillis()

                                    // API 연결 및 이벤트 토픽 발행
                                    viewModelScope.launch {
                                        try {
                                            // Get Presigned URL
                                            val urlResponse =
                                                imageRepository.getPresignedUrl(imageName)

                                            // Presigned URL 이미지 업로드
                                            val uploadResult =
                                                imageRepository
                                                    .uploadToPresignedUrl(
                                                        urlResponse.body.presignedUrl,
                                                        imageData
                                                    )

                                            if (!uploadResult) {
                                                Log.e(tag, "S3 Image Upload Failed")
                                                return@launch
                                            }

                                            imageRepository.saveImage(
                                                urlResponse.body.imageName,
                                                urlResponse.body.imagePath
                                            )

                                            // BoundingBoxUtils 데이터 추출
                                            val (objectType, confidence) =
                                                BoundingBoxUtils.getTypeAndConfidence(boundingBoxes)

                                            onResult(
                                                DetectionManager.getNextTrackingId(),
                                                objectType,
                                                confidence,
                                                BoundingBoxUtils.getCoordinates(boundingBoxes)
                                            )
                                        } catch (e: Exception) {
                                            Log.e(tag, "Event Image Upload Failed: ", e)
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Frame processing error", e)
            } finally {
                currentFrame?.release()
            }
        }
    }

    private fun createImage(bitmap: Bitmap, quality: Int = 80): Pair<String, ByteArray> {
        // 현재 시간을 기반으로 동적 이미지 이름 생성
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageName = "image_$timestamp.jpg" // 이미지 이름 (예: image_20241207_123456.jpg)

        // Bitmap을 JPEG 포맷으로 변환
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()

        return Pair(imageName, byteArray)
    }
}

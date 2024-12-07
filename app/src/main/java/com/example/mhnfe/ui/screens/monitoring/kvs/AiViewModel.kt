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
import com.example.mhnfe.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import org.opencv.core.Rect
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.ByteArrayOutputStream



@HiltViewModel
class AiViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // Context 주입
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val tag = "AiViewModel"
    private val motionDetector = MotionDetector()
    private val yoloDetectionManager = YoloDetectionManager(context)
    private var lastEventTime: Long = 0 // 마지막 이벤트 발생 시간 기록
    private val eventDelayMillis = 500L // event data to iot 딜레이 시간

    fun processFrame(bitmap: Bitmap?, onResult: (Int, String, String, String) -> Unit) {
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
                                val imageResult = createImage(bmp)
                                val imageName = imageResult.first // 이미지 이름
                                val imageData = imageResult.second // JPEG 포맷 이미지 데이터

                                if (BoundingBoxUtils.shouldTriggerEvent(lastEventTime, eventDelayMillis)) {
                                    lastEventTime = System.currentTimeMillis()
                                    Log.d("DetectEvent", "detectEvent() 시작")

                                    //api 연결
                                    viewModelScope.launch {
                                        try {
                                            //Presigned URL 가져오기
                                            val urlResponse =
                                                imageRepository.getPresignedUrl(imageName)

                                            //Presigned URL 이미지 업로드
                                            val uploadResult = imageRepository.uploadToPresignedUrl(
                                                urlResponse.body.presignedUrl,
                                                imageData
                                            )

                                            if (uploadResult) {
                                                imageRepository.saveImage(
                                                    imageName = urlResponse.body.imageName,
                                                    imagePath = urlResponse.body.imagePath
                                                )
                                                Log.e("PresignedURL", "이미지 저장 성공")
                                            }

                                            val trackingId = DetectionManager.getNextTrackingId()
                                            // BoundingBoxUtil을 사용하여 JSON 데이터 생성
                                            val coordinatesJson =
                                                BoundingBoxUtils.generateCoordinatesJson(
                                                    boundingBoxes
                                                )
                                            val objectNameJson =
                                                BoundingBoxUtils.generateObjectNameJson(
                                                    boundingBoxes
                                                )
                                            val confidenceJson =
                                                BoundingBoxUtils.generateConfidenceJson(
                                                    boundingBoxes
                                                )

                                            onResult(
                                                trackingId,
                                                coordinatesJson,
                                                objectNameJson,
                                                confidenceJson
                                            )
                                        }catch (e: Exception) {
                                            Log.e(tag, "Image upload failed", e)
                                        }
                                    }
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

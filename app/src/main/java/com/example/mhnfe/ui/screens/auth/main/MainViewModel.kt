package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.CctvSelfInfoResponse
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.domain.repository.QRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val qrRepository: QRRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val cctvResponseDataStore: DataStore<CCTVResponseBody>,
//    private val tokenManager: TokenManager
) : ViewModel() {

    private val _cctvInfo = MutableStateFlow<CctvSelfInfoResponse?>(null)
    val cctvInfo = _cctvInfo.asStateFlow()

    fun autoLogin(
        onSuccess: (String, Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Access Token 가져오기
                val accessToken = accessTokenDataStore.data.map { it.accessToken }.first()

                // 2. Access Token으로 그룹과 역할을 조회
                val response = groupRepository.getGroupMember(accessToken)

                // 3. 성공 시 그룹 아이디와 역할을 리턴
                if(response.isSuccessful){
                    response.body()?.body?.let { onSuccess(it.role, it.groupId) }
                } else {
                    onSuccess("", 0L)
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "AutoLogin Failed... ${e.message}")
            }
        }
    }

    fun fetchCctvId(
        onSuccess: (CctvSelfInfoResponse) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val cctvInfo = qrRepository.getCctvInfo()
                _cctvInfo.value = cctvInfo
                onSuccess(cctvInfo)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun getCctvAccessToken(
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val cctvAccessToken = cctvResponseDataStore.data.map { it.accessToken }.first()
            onSuccess(cctvAccessToken)
        }
    }
}

//@HiltViewModel
//class AiViewModel @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val imageRepository: ImageRepository // ImageRepository 주입
//) : ViewModel() {
//    private val tag = "AiViewModel"
//    private val motionDetector = MotionDetector()
//    private val yoloDetectionManager = YoloDetectionManager(context)
//    private var lastEventTime: Long = 0
//    private val eventDelayMillis = 500L
//
//    fun processFrame(bitmap: Bitmap?, onResult: (Int, String, String, String) -> Unit) {
//        viewModelScope.launch {
//            try {
//                bitmap?.let { bmp ->
//                    val currentFrame: Mat = BitmapToMatConverter.BitToMat(bmp)
//                    val motionAreas: List<Rect> = motionDetector.detectMotion(currentFrame)
//
//                    if (motionAreas.isNotEmpty()) {
//                        Log.d(tag, "Motion detected in ${motionAreas.size} area(s)")
//
//                        yoloDetectionManager.detect(bmp) { boundingBoxes, _ ->
//                            if (boundingBoxes.isNotEmpty()) {
//                                val imageResult = createImage(bmp)
//                                val imageName = imageResult.first
//                                val imageData = imageResult.second
//
//                                if (BoundingBoxUtils.shouldTriggerEvent(lastEventTime, eventDelayMillis)) {
//                                    lastEventTime = System.currentTimeMillis()
//                                    Log.d("DetectEvent", "detectEvent() 시작")
//
//                                    // 이미지 업로드 프로세스 시작
//                                    viewModelScope.launch {
//                                        try {
//                                            // 1. Presigned URL 가져오기
//                                            val urlResponse = imageRepository.getPresignedUrl(imageName)
//
//                                            // 2. Presigned URL로 이미지 업로드
//                                            val uploadResult = uploadImageToPresignedUrl(
//                                                urlResponse.body.presignedUrl,
//                                                imageData
//                                            )
//
//                                            // 3. 업로드 성공 시 이미지 정보 저장
//                                            if (uploadResult) {
//                                                imageRepository.saveImage(
//                                                    imageName = urlResponse.body.imageName,
//                                                    imagePath = urlResponse.body.imagePath
//                                                )
//                                            }
//
//                                            val trackingId = DetectionManager.getNextTrackingId()
//                                            val coordinatesJson = BoundingBoxUtils.generateCoordinatesJson(boundingBoxes)
//                                            val objectNameJson = BoundingBoxUtils.generateObjectNameJson(boundingBoxes)
//                                            val confidenceJson = BoundingBoxUtils.generateConfidenceJson(boundingBoxes)
//
//                                            onResult(trackingId, coordinatesJson, objectNameJson, confidenceJson)
//                                        } catch (e: Exception) {
//                                            Log.e(tag, "Image upload failed", e)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    currentFrame.release()
//                }
//            } catch (e: Exception) {
//                Log.e(tag, "Frame processing error", e)
//            }
//        }
//    }
//
//    private fun createImage(bitmap: Bitmap, quality: Int = 80): Pair<String, ByteArray> {
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val imageName = "image_$timestamp.jpg"
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//        val byteArray = outputStream.toByteArray()
//        return Pair(imageName, byteArray)
//    }
//
//    private suspend fun uploadImageToPresignedUrl(presignedUrl: String, imageData: ByteArray): Boolean {
//        return try {
//            withContext(Dispatchers.IO) {
//                val url = URL(presignedUrl)
//                val connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "PUT"
//                connection.doOutput = true
//                connection.setRequestProperty("Content-Type", "image/jpeg")
//
//                connection.outputStream.use { outputStream ->
//                    outputStream.write(imageData)
//                }
//
//                val responseCode = connection.responseCode
//                responseCode == HttpURLConnection.HTTP_OK
//            }
//        } catch (e: Exception) {
//            Log.e(tag, "Failed to upload image to presigned URL", e)
//            false
//        }
//    }
//}
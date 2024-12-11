package com.example.mhnfe.ui.screens.qr.qrgenerate

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.di.UserType
import com.example.mhnfe.domain.repository.GroupRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class QRScreenUiState(
    val title: String = "",
    val message: String = "",
    val qrContent: String = "",
    val qrBitmap: ImageBitmap? = null,
    val userType: UserType = UserType.CCTV,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QRViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(QRScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun setUserType(type: UserType) {
        _uiState.update { currentState ->
            currentState.copy(
                userType = type,
                title = when (type) {
                    UserType.CCTV -> "CCTV 등록"
                    UserType.VIEWER -> "뷰어 등록"
                    UserType.MASTER -> ""
                },
                message = when (type) {
                    UserType.CCTV -> "CCTV로 사용할 기기에서\nQR 인증을 해주세요"
                    UserType.VIEWER -> "뷰어로 사용할 기기에서\nQR 인증을 해주세요"
                    UserType.MASTER -> ""
                }
            )
        }
        generateQRContent()
    }

    private fun generateQRContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = when (_uiState.value.userType) {
                    UserType.CCTV -> groupRepository.generateCctvQR()
                    UserType.VIEWER -> groupRepository.generateViewerQR()
                    else -> null
                }
                Log.d("QRViewModel", "API response received: $response")

                response?.let { apiResponse ->
                    if (apiResponse.result.code == 200) {
                        val jsonContent = when (_uiState.value.userType) {
                            UserType.CCTV -> JSONObject().apply {
                                put("groupId", apiResponse.body.groupId)
                                put("kvsChannelId", apiResponse.body.kvsChannelId)
                            }.toString()
                            UserType.VIEWER -> JSONObject().apply {
                                put("groupId", apiResponse.body.groupId)
                            }.toString()
                            else -> ""
                        }

                        _uiState.update { it.copy(
                            qrContent = jsonContent,
                            isLoading = false
                        ) }
                    } else {
                        _uiState.update { it.copy(
                            error = apiResponse.result.message,
                            isLoading = false
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "QR 코드 생성 중 오류가 발생했습니다.: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun generateQRBitmap(size: Int): Bitmap {
        val content = uiState.value.qrContent

        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(
                content,  // qrContent 대신 content 사용
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            canvas.drawColor(android.graphics.Color.WHITE)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
            }

            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix.get(x, y)) {
                        canvas.drawRect(
                            x.toFloat(),
                            y.toFloat(),
                            (x + 1).toFloat(),
                            (y + 1).toFloat(),
                            paint
                        )
                    }
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            bitmap
        }
    }
}
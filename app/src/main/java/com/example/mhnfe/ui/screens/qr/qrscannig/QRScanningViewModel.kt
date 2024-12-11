package com.example.mhnfe.ui.screens.qr.qrscannig

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.data.remote.response.CctvQRResponse
import com.example.mhnfe.data.remote.response.ViewerQRResponse
import com.example.mhnfe.di.UserType
import com.example.mhnfe.domain.repository.QRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

sealed class QRScanNavigationEvent {
    data class NavigateToCCTV(
        val channelName: String,
        val role: ChannelRole = ChannelRole.MASTER
    ) : QRScanNavigationEvent()

    data class NavigateToViewer(val userType: UserType) : QRScanNavigationEvent()
}


@HiltViewModel
class QRScanningViewModel @Inject constructor(
    private val qrRepository: QRRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRScanUiState())
    val uiState: StateFlow<QRScanUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<QRScanNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun setUserType(type: UserType) {
        _uiState.update { it.copy(userType = type) }
    }

    fun onQRCodeScanned(result: String) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        qrCodeResult = result,
                        showMessage = "QR 코드를 처리중입니다..."
                    )
                }

                when (_uiState.value.userType) {
                    UserType.CCTV -> {
                        val qrInfo = parseCCTVQRData(result)
                        processCCTVQR(qrInfo)
                        _navigationEvent.send(QRScanNavigationEvent.NavigateToCCTV(
                            channelName = qrInfo.kvsChannelName
                        ))
                    }
                    UserType.VIEWER -> {
                        processViewerQR(result)
                        _navigationEvent.send(QRScanNavigationEvent.NavigateToViewer(UserType.VIEWER))
                    }
                    null -> throw IllegalStateException("사용자 타입이 설정되지 않았습니다.")
                    UserType.MASTER -> TODO()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    private fun parseCCTVQRData(qrData: String): QRData {
        return try {
            val json = JSONObject(qrData)

            Log.d("QRScanningViewModel", "QR Data: $qrData")
            Log.d("QRScanningViewModel", "Parsed groupId: ${json.getInt("groupId")}")
            Log.d("QRScanningViewModel", "Parsed kvsChannelName: ${json.getString("kvsChannelId")}")

            QRData(
                groupId = json.getInt("groupId"),
                kvsChannelName = json.getString("kvsChannelId")  // QR에서는 kvsChannelId로 읽고
            )
        } catch (e: JSONException) {
            throw IllegalArgumentException("CCTV QR 코드 형식이 올바르지 않습니다")
        }
    }

    private suspend fun processCCTVQR(qrInfo: QRData) {
        try {
            val response = qrRepository.generateCctvQR(
                groupId = qrInfo.groupId,
                kvsChannelName = qrInfo.kvsChannelName
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    cctvResponse = response,
                    showMessage = "CCTV QR 코드가 성공적으로 등록되었습니다."
                )
            }
        } catch (e: Exception) {
            handleError(e)
            // 에러가 발생해도 네비게이션 이벤트 전송
            _navigationEvent.send(QRScanNavigationEvent.NavigateToCCTV(
                channelName = qrInfo.kvsChannelName
            ))
        }
    }

    private fun parseViewerQRData(qrData: String): Int {
        return try {
            val json = JSONObject(qrData)
            json.getInt("groupId")
        } catch (e: JSONException) {
            throw IllegalArgumentException("뷰어 QR 코드 형식이 올바르지 않습니다")
        }
    }

    private suspend fun processViewerQR(qrData: String) {
        val groupId = parseViewerQRData(qrData)

        val response = qrRepository.generateViewerQR(groupId)

        _uiState.update {
            it.copy(
                isLoading = false,
                viewerResponse = response,
                showMessage = "뷰어 QR 코드가 성공적으로 등록되었습니다."
            )
        }
    }

    private fun handleError(e: Throwable) {
        val errorMessage = when (e) {
            is HttpException -> when (e.code()) {
                400 -> "잘못된 QR 코드입니다"
                401 -> "인증에 실패했습니다"
                404 -> "찾을 수 없는 그룹입니다"
                else -> "서버 오류가 발생했습니다: ${e.code()}"
            }
            is IllegalArgumentException -> e.message ?: "QR 코드 형식이 올바르지 않습니다"
            is IllegalStateException -> e.message ?: "처리할 수 없는 상태입니다"
            else -> "오류가 발생했습니다: ${e.message}"
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorMessage,
                showMessage = "QR 코드 처리 중 오류가 발생했습니다."
            )
        }
        Log.e("QRScanningViewModel", "QR 처리 실패", e)
    }

    fun resetState() {
        _uiState.update {
            QRScanUiState(userType = it.userType)
        }
    }
}

data class QRScanUiState(
    val userType: UserType? = null,
    val isLoading: Boolean = false,
    val qrCodeResult: String? = null,
    val error: String? = null,
    val showMessage: String? = null,
    val cctvResponse: CctvQRResponse? = null,
    val viewerResponse: ViewerQRResponse? = null
)

private data class QRData(
    val groupId: Int,
    val kvsChannelName: String
)

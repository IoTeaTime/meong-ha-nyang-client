//package com.example.mhnfe.ui.screens.qr.qrscannig
//
//import androidx.datastore.core.DataStore
//import androidx.lifecycle.ViewModel
//import com.example.mhnfe.data.remote.api.GroupApi
//import com.example.mhnfe.data.remote.response.AccessToken
//import com.example.mhnfe.di.UserType
//import com.example.mhnfe.ui.screens.qr.qrgenerate.QRScreenUiState
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import javax.inject.Inject
//
//data class QRScanUiState(
//    val userType: UserType? = null,
//    val isLoading: Boolean = false,
//    val error: String? = null,
//    val cctvResponse: CCTVQRResponse? = null,
//    val viewerResponse: ViewerQRResponse? = null,
//    val qrCodeResult: String? = null,
//    val showMessage: String? = null
//)
//@HiltViewModel
//class QRScanningViewModel @Inject constructor(
//    private val accessTokenDataStore: DataStore<AccessToken>,
//    private val groupApi: GroupApi
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(QRScanUiState())
//    val uiState: StateFlow<QRScanUiState> = _uiState.asStateFlow()
//
//    fun setUserType(type: UserType) {
//        _uiState.update { it.copy(userType = type) }
//    }
//
//    fun onQRCodeScanned(result: String) {
//        viewModelScope.launch {
//            try {
//                _uiState.update {
//                    it.copy(
//                        isLoading = true,
//                        qrCodeResult = result,
//                        showMessage = "QR 코드를 처리중입니다..."
//                    )
//                }
//
//                when (_uiState.value.userType) {
//                    UserType.CCTV -> processCCTVQR(result)
//                    UserType.VIEWER -> processViewerQR(result)
//                    else -> throw IllegalStateException("지원하지 않는 사용자 타입입니다.")
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "오류가 발생했습니다: ${e.message}",
//                        showMessage = "QR 코드 처리 중 오류가 발생했습니다."
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun processCCTVQR(qrData: String) {
//        // QR 데이터에서 필요한 정보 추출
//        val (groupId, thingId) = parseQRData(qrData)
//
//        // CCTV API 호출
//        val response = groupApi.openCCTV(
//            CCTVRequest(
//                groupId = groupId,
//                thingId = thingId
//            )
//        )
//
//        if (response.isSuccessful) {
//            response.body()?.let { cctvResponse ->
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        cctvResponse = cctvResponse,
//                        showMessage = "CCTV 연결이 준비되었습니다."
//                    )
//                }
//            }
//        } else {
//            throw Exception("API 호출 실패: ${response.message()}")
//        }
//    }
//
//    private suspend fun processViewerQR(qrData: String) {
//        // Viewer용 QR 데이터에서 groupId 추출
//        val groupId = parseViewerQRData(qrData)
//
//        // Viewer API 호출
//        val response = groupApi.connectViewer(groupId)
//
//        if (response.isSuccessful) {
//            response.body()?.let { viewerResponse ->
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        viewerResponse = ViewerQRResponse(groupId = groupId),
//                        showMessage = "뷰어 연결이 준비되었습니다."
//                    )
//                }
//            }
//        } else {
//            throw Exception("API 호출 실패: ${response.message()}")
//        }
//    }
//
//    // QR 데이터 파싱 함수들
//    private fun parseQRData(qrData: String): Pair<Int, String> {
//        // QR 코드 데이터 파싱 로직 구현
//        // 예시: "groupId:123,thingId:abc" 형식이라고 가정
//        val params = qrData.split(",").associate {
//            val (key, value) = it.split(":")
//            key to value
//        }
//
//        return Pair(
//            params["groupId"]?.toIntOrNull() ?: throw Exception("잘못된 groupId 형식"),
//            params["thingId"] ?: throw Exception("thingId가 없습니다")
//        )
//    }
//
//    private fun parseViewerQRData(qrData: String): Int {
//        // Viewer QR 코드에서 groupId만 추출
//        return qrData.toIntOrNull() ?: throw Exception("잘못된 groupId 형식")
//    }
//
//    fun resetState() {
//        _uiState.update {
//            QRScanUiState(userType = it.userType)
//        }
//    }
//}

package com.example.mhnfe.ui.screens.qr.qrscannig

import androidx.lifecycle.ViewModel

class QRScanningViewModel : ViewModel() {
    private var _qrCodeResult: String? = null
    val qrCodeResult: String?
        get() = _qrCodeResult

    private var _showMessage: String? = null
    val showMessage: String?
        get() = _showMessage

    fun onQRCodeScanned(result: String) {
        // QR 코드가 스캔되었을 때 호출되는 메소드
        _qrCodeResult = result
        _showMessage = "QR 코드가 인식되었습니다: $result" // 메시지 설정

        // 필요한 추가 작업 수행
    }

    fun resetQRCodeResult() {
        // QR 코드 결과를 초기화하는 메소드
        _qrCodeResult = null
        _showMessage = null // 메시지 초기화
    }
}
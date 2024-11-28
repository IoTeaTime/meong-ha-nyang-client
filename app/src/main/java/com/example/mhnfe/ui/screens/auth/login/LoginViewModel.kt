package com.example.mhnfe.ui.screens.auth.login

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.FCMResponse
import com.example.mhnfe.data.remote.response.LoginResponse
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val refreshTokenDataStore: DataStore<RefreshToken>,
    private val loginRequestDataStore: DataStore<LoginRequest>
) : ViewModel() {

    // 로그인 결과 상태
    private val _loginResponse = MutableStateFlow<LoginResponse?>(null)
    val loginResponse: StateFlow<LoginResponse?> = _loginResponse

    // FCM 토큰 전송 결과 상태
    private val _apiResponse = MutableStateFlow<FCMResponse?>(null)

    // 에러 메시지 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 사용자 친화적인 에러 메시지를 매핑하는 함수
    private fun mapErrorMessage(code: Int, message: String, description: String?): String {
        return when (message) {
            "NOT FOUND" -> "회원 정보를 찾을 수 없습니다. 다시 확인해주세요."
            "BAD REQUEST" -> "비밀번호가 일치하지 않습니다. 다시 입력해주세요."
            else -> "알 수 없는 오류가 발생했습니다.\n에러 코드: $code\n$description"
        }
    }

    // 로그인 함수
    fun loginUser(email: String, password: String, isAutoLogin: Boolean) {
        viewModelScope.launch {
            try {
                // 서버로 로그인 요청
                val response = authRepository.login(email,password)

                if (response.result.code == 200) {
                    // JWT 엑세스, 리프레시 토큰 저장
                    saveTokens(
                        response.body.accessToken.toString(),
                        response.body.refreshToken.toString()
                    )
                    Log.d("LoginViewModel","response: " + response.body.accessToken)

                    // 자동 로그인 정보 저장 (isAutoLogin이 true일 경우)
                    if (isAutoLogin) {
                        saveAutoLoginInfo(email, password)
                    }

                    _loginResponse.value = response
                    _errorMessage.value = null
                } else {
                    Log.d("LoginViewModel","response: " + response.result.message)
                    // 실패한 경우 사용자 친화적인 에러 메시지 생성
                    _errorMessage.value = mapErrorMessage(
                        response.result.code,
                        response.result.message,
                        response.result.description
                    )
                    Log.e("LoginViewModel","Error: " + _errorMessage.value)
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                Log.e("LoginViewModel", "Login error", e)
                _errorMessage.value = "로그인 중 오류가 발생했습니다. ${e.message}"
            }
        }
    }

    // 엑세스 토큰과 리프레시 토큰 저장
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        // 엑세스 토큰 저장
        accessTokenDataStore.updateData { currentToken ->
            currentToken.copy(accessToken = accessToken)
        }
        // 리프레시 토큰 저장
        refreshTokenDataStore.updateData { currentToken ->
            currentToken.copy(refreshToken = refreshToken)
        }
        Log.d("LoginViewModel", "엑세스 토큰 및 리프레시 토큰 저장 완료.")
    }

    fun getAccessToken(onTokenRetrieved: (String?) -> Unit) {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            onTokenRetrieved(token)
        }
    }

    // FCM 토큰 서버로 보내는
    fun refreshFcmToken(jwtToken: String, fcmToken: String) {
        viewModelScope.launch {
            try {
                // 서버로 토큰 전송 요청
                val response = authRepository.refreshFcmToken(jwtToken, fcmToken)

                Log.d("LoginViewModel","response: " + response.result.message)
                if (response.result.code == 200) {
                    // 성공적으로 로그인한 경우
                    _apiResponse.value = response
                    Log.d("LoginViewModel","_apiResponse.value: " + _apiResponse.value)
                    _errorMessage.value = null
                } else {
                    Log.d("LoginViewModel","response: " + response.result.message)
                    // 실패한 경우 사용자 친화적인 에러 메시지 생성
                    _errorMessage.value = mapErrorMessage(
                        response.result.code,
                        response.result.message,
                        response.result.description
                    )
                    Log.e("LoginViewModel","Error: " + _errorMessage.value)
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                Log.e("LoginViewModel", "Login error", e)
                _errorMessage.value = "FCM 토큰 전송 중 오류가 발생했습니다. ${e.message}"
            }
        }
    }

    // ViewModel에 자동 로그인 정보 저장 메서드 추가
    suspend fun saveAutoLoginInfo(id: String, password: String) {
        // DataStore에 자동 로그인 정보 저장
        loginRequestDataStore.updateData { currentLoginInfo ->
            currentLoginInfo.copy(email = id, password = password)
        }
        Log.d("LoginViewModel", "자동 로그인 정보 저장 완료: $id, $password")
    }
}

package com.example.mhnfe.ui.screens.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.model.LoginResponse
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.utils.TokenManager
import com.google.android.gms.common.api.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // 로그인 결과 상태
    private val _loginResponse = MutableStateFlow<LoginResponse?>(null)
    val loginResponse: StateFlow<LoginResponse?> = _loginResponse
    // FCM 토큰 전송 결과 상태
    private val _apiResponse = MutableStateFlow<ApiResponse?>(null)

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
    fun loginUser(editor: SharedPreferences.Editor, email: String, password: String) {
        viewModelScope.launch {
            try {
                // 서버로 로그인 요청
                val response = authRepository.login(email, password)

                val accessToken = response.body.accessToken
                val refreshToken = response.body.refreshToken
                val accessTokenExpiration = System.currentTimeMillis() + (2 * 60 * 60 * 1000)
                val refreshTokenExpiration =
                    System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)

                // JWT 엑세스, 리프레시 토큰, expiration time 저장
                editor.putString("accessToken", accessToken)
                editor.putString("refreshToken", refreshToken)
                editor.putLong("accessTokenExpiration", accessTokenExpiration)
                editor.putLong("refreshTokenExpiration", refreshTokenExpiration)
                editor.commit()
                Log.d("LoginViewModel", "JWT 엑세스, 리프레시 토큰 저장 완료: $accessToken")

                Log.d("LoginViewModel","response: " + response.result.message)
                if (response.result.code == 200) {
                    // 성공적으로 로그인한 경우
                    _loginResponse.value = response
                    Log.d("LoginViewModel","_loginResponse.value: " + _loginResponse.value)
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
}

package com.example.mhnfe.ui.screens.auth.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.data.remote.response.SignUpResponse
import com.example.mhnfe.data.remote.response.CheckEmailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _signUpResponse = MutableStateFlow<SignUpResponse?>(null)
    val signUpResponse: StateFlow<SignUpResponse?> = _signUpResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _emailCheckResponse = MutableStateFlow<CheckEmailResponse?>(null)
    val emailCheckResponse: StateFlow<CheckEmailResponse?> = _emailCheckResponse

    suspend fun checkEmailStatus(email: String): Pair<Int, String> {
//        Log.d("SignUpScreen", "checkEmailDuplicate 호출, email=$email")
        return try {
            // API call
            val response = authRepository.checkEmailDuplicate(email)
//            Log.d("SignUpScreen", "checkEmailDuplicate 응답1: $response")

            // Response handling
            when (response.result.code) {
                200 -> Pair(200, response.result.description ?: "사용 가능한 이메일입니다.")
                400 -> Pair(400, response.result.description ?: "중복된 이메일입니다.")
                else -> Pair(response.result.code, response.result.description ?: "알 수 없는 상태")
            }
        } catch (e: HttpException) {
            // HTTP exception handling
            val statusCode = e.code()
            val errorMessage = e.message ?: "알 수 없는 오류"
            Log.e("SignUpScreen", "checkEmailStatus: HTTP 예외 발생 - 코드: $statusCode, 메시지: $errorMessage", e)

            Pair(statusCode, "HTTP 오류 발생: $errorMessage")
        }
    }

    fun signUpUser(email: String, password: String, passwordConfirm: String, nickname: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.signUp(email, password, passwordConfirm, nickname)
                if (response.result?.code == 0) {
                    _signUpResponse.value = response
                } else {
                    _errorMessage.value = response.result?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "회원가입 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }
}

package com.example.mhnfe.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.data.model.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _signUpResponse = MutableStateFlow<ApiResponse?>(null)
    val signUpResponse: StateFlow<ApiResponse?> = _signUpResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _emailCheckResponse = MutableStateFlow<ApiResponse?>(null)
    val emailCheckResponse: StateFlow<ApiResponse?> = _emailCheckResponse

    fun checkEmailDuplicate(email: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.checkEmailDuplicate(email)
                if (response.result?.code == 0) {
                    _emailCheckResponse.value = response  // Save the result of the email duplicate check if the email is not duplicated
                } else {
                    _errorMessage.value = "이미 사용 중인 이메일입니다: ${response.result.message}" // Show an error message in case of email duplication
                }
            } catch (e: Exception) {
                _errorMessage.value = "이메일 중복 확인 중 오류가 발생했습니다: ${e.message}"
            }
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

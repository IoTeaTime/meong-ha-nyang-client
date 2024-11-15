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

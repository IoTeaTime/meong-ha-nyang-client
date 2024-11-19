package com.example.mhnfe.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.model.ApiResponse
import com.example.mhnfe.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResponse = MutableStateFlow<ApiResponse?>(null)
    val loginResponse: StateFlow<ApiResponse?> = _loginResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.login(email, password)
                if (response.result.code == 0) {
                    _loginResponse.value = response
                } else {
                    _errorMessage.value = response.result.message
                }
            } catch (e: Exception) {
                _errorMessage.value = "로그인에 실패 했습니다: ${e.message}"
            }
        }
    }
}
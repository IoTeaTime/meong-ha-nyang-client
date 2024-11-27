package com.example.mhnfe.ui.screens.auth.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun autoLogin(
        savedId: String?,
        savedPassword: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!savedId.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            // 저장된 로그인 정보로 자동 로그인 시도
            viewModelScope.launch {
                try {
                    val response = authRepository.login(savedId, savedPassword)
                    if (response.result.code == 200) {
                        // 자동 로그인 성공
                        onSuccess()
                    }
                } catch (e: Exception) {
                    // 로그인 실패 시 오류 처리
                    onFailure(e)
                }
            }
        }
    }
}
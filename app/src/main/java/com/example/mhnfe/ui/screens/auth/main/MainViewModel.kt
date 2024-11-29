package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginRequestDataStore: DataStore<LoginRequest>,
    private val cctvResponseDataStore: DataStore<CCTVResponseBody>
) : ViewModel() {
    fun autoLogin(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("MainViewModel", "Start AutoLogin")

        viewModelScope.launch {
            try {
                val loginRequest = loginRequestDataStore.data.first() // 초기 값 읽기
                val savedId = loginRequest.email
                val savedPassword = loginRequest.password

                if (!savedId.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                    // 로그인 시도
                    val response = authRepository.login(savedId, savedPassword)
                    Log.d("MainViewModel", "AutoLogin data :$savedId $savedPassword")
                    if (response.result.code == 200) {
                        Log.d("MainViewModel", "AutoLogin Success!!")
                        onSuccess()
                    } else {
                        Log.d("MainViewModel", "AutoLogin Failed: Invalid Credentials")
                        onFailure(Exception("Invalid credentials"))
                    }
                } else {
                    // 저장된 데이터가 없거나 비어 있는 경우
                    Log.d("MainViewModel", "AutoLogin Failed: No valid saved credentials")
                    onFailure(Exception("No valid saved credentials"))
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "AutoLogin Failed...")
                onFailure(e)
            }
        }
    }

    // CCTV ID를 불러오는 메서드
    fun fetchCctvId(onSuccess: (Int) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                val cctvId = cctvResponseDataStore.data.first().cctvId
                onSuccess(cctvId)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }


}
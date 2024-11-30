package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.data.remote.response.LoginResponse
import com.example.mhnfe.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupIdDataStore: DataStore<GroupId>,
    private val loginRequestDataStore: DataStore<LoginRequest>
) : ViewModel() {
    fun autoLogin(
        onSuccess: (String, Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("MainViewModel", "Start AutoLogin")

        viewModelScope.launch {
            try {
                val loginRequest = loginRequestDataStore.data.first() // 초기 값 읽기
                val groupId = groupIdDataStore.data.map { it.groupId }.first()
                val savedId = loginRequest.email
                val savedPassword = loginRequest.password

                if (!savedId.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                    // 로그인 시도
                    val response = authRepository.login(savedId, savedPassword)
                    Log.d("MainViewModel", "AutoLogin data :$savedId $savedPassword")
                    if (response.result.code == 200) {
                        Log.d("MainViewModel", "AutoLogin Success!!")
                        onSuccess(response.body.role, groupId)
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
}
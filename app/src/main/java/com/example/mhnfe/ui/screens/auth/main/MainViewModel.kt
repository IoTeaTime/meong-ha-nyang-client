package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.manager.TokenManager
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.data.remote.response.LoginResponse
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.domain.repository.QRRepository
import com.example.mhnfe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val qrRepository: QRRepository,
    private val groupIdDataStore: DataStore<GroupId>,
    private val loginRequestDataStore: DataStore<LoginRequest>,
    private val refreshTokenDataStore: DataStore<RefreshToken>,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _cctvInfo = MutableStateFlow<CctvInfoResponse?>(null)
    val cctvInfo = _cctvInfo.asStateFlow()
    fun autoLogin(
        onSuccess: (String, Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val refreshToken = refreshTokenDataStore.data.map { it.refreshToken }.first()
                Log.d("MainViewModel","Get New AccessToken : ${refreshToken}")
                val response = userRepository.getNewAccessToken(refreshToken)
                Log.d("MainViewModel","Get New AccessToken : ${response}")
                val groupId = groupIdDataStore.data.map { it.groupId }.first()

                if (response.isSuccessful) {
                    // 로그인 시도
                    val accessToken = response.body()?.body?.accessToken
                    if (accessToken != null) {
                        tokenManager.saveAccessToken(accessToken)
                    }
                    val userResponse = accessToken?.let { groupRepository.getGroupMember(it) }
                    if (userResponse != null) {
                        Log.d("MainViewModel", "AutoLogin data :${userResponse.code()}")
                    }
                    if (userResponse != null) {
                        if (userResponse.code() == 200) {
                            Log.d("MainViewModel", "AutoLogin Success!!")
                            userResponse.body()?.body?.let { onSuccess(it.role, groupId) }
                        } else {
                            Log.d("MainViewModel", "AutoLogin Failed: Invalid Credentials")
                            onFailure(Exception("Invalid credentials"))
                        }
                    }
                } else {
                    // 저장된 데이터가 없거나 비어 있는 경우
                    Log.d("API Error", "Error: ${response.code()}, Message: ${response.message()}")
                    Log.d("MainViewModel", "AutoLogin Failed: No valid saved credentials")
                    onFailure(Exception("No valid saved credentials"))
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "AutoLogin Failed... ${e.message}")
                onFailure(e)
            }
        }
    }
    fun fetchCctvId(
        onSuccess: (CctvInfoResponse) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val cctvInfo = qrRepository.getCctvInfo()
                _cctvInfo.value = cctvInfo
                onSuccess(cctvInfo)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}
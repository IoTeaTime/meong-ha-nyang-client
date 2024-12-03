package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.manager.TokenManager
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.domain.repository.QRRepository
import com.example.mhnfe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val qrRepository: QRRepository,
    private val groupIdDataStore: DataStore<GroupId>,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val refreshTokenDataStore: DataStore<RefreshToken>,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _cctvInfo = MutableStateFlow<CctvInfoResponse?>(null)
    val cctvInfo = _cctvInfo.asStateFlow()
    fun autoLogin(
        isAutoLoginEnabled: Boolean,
        onSuccess: (String, Long) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!isAutoLoginEnabled) {
                    throw Exception("Auto login is disabled")
                }

                // 1. Access Token 가져오기
                val accessToken = accessTokenDataStore.data.map { it.accessToken }.first()

                // 2. Access Token으로 그룹과 역할을 조회
                val response = groupRepository.getGroupMember(accessToken)

                // 3. 성공 시 그룹 아이디와 역할을 리턴
                if(response.isSuccessful){
                    response.body()?.body?.let { onSuccess(it.role, it.groupId) }
                }

                // 4. 실패 시 Refresh Token 으로 Access Token 재발급
                else {
                    try {
                        // TokenManager를 사용하여 리프레시 토큰으로 새로운 액세스 토큰을 갱신
                        val newAccessToken = tokenManager.refreshAccessToken()

                        // 새로 받은 액세스 토큰으로 다시 그룹 조회
                        val newResponse = groupRepository.getGroupMember(newAccessToken)

                        if (newResponse.isSuccessful) {
                            newResponse.body()?.body?.let { onSuccess(it.role, it.groupId) }
                        } else {
                            Log.d("MainViewModel", "AutoLogin Failed: Invalid Credentials")
                            onFailure(Exception("Invalid credentials"))
                        }
                    } catch (e: Exception) {
                        // 새 액세스 토큰을 얻을 수 없을 경우
                        Log.d("MainViewModel", "AutoLogin Failed: ${e.message}")
                        onFailure(e)
                    }
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
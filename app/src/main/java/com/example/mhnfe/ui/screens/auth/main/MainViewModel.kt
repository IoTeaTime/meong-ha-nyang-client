package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CCTVResponseBody
import com.example.mhnfe.data.remote.response.CctvInfoResponse
import com.example.mhnfe.data.remote.response.CctvSelfInfoResponse
import com.example.mhnfe.domain.repository.GroupRepository
import com.example.mhnfe.domain.repository.QRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val qrRepository: QRRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val cctvResponseDataStore: DataStore<CCTVResponseBody>,
//    private val tokenManager: TokenManager
) : ViewModel() {

    private val _cctvInfo = MutableStateFlow<CctvSelfInfoResponse?>(null)
    val cctvInfo = _cctvInfo.asStateFlow()

    fun autoLogin(
        onSuccess: (String, Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Access Token 가져오기
                val accessToken = accessTokenDataStore.data.map { it.accessToken }.first()

                // 2. Access Token으로 그룹과 역할을 조회
                val response = groupRepository.getGroupMember(accessToken)

                // 3. 성공 시 그룹 아이디와 역할을 리턴
                if(response.result.code == 200){
                    onSuccess(response.body.role, response.body.groupId)
                }
                else if(response.result.code == 404) {
                    onSuccess("", 0L)
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    // 404를 별도로 처리
                    onSuccess("", 0L)
                } else {
                    Log.e("MainViewModel", "AutoLogin Failed... ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "AutoLogin Failed... ${e.message}")
            }
        }
    }

    fun fetchCctvId(
        onSuccess: (CctvSelfInfoResponse) -> Unit,
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

    fun getCctvAccessToken(
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val cctvAccessToken = cctvResponseDataStore.data.map { it.accessToken }.first()
            onSuccess(cctvAccessToken)
        }
    }
}


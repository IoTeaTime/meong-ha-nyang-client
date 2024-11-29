package com.example.mhnfe.ui.screens.mypage

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.DeleteResponse
import com.example.mhnfe.data.remote.response.LogoutResponse
import com.example.mhnfe.data.remote.response.RefreshToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userApi: UserApi,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val refreshTokenDataStore: DataStore<RefreshToken>, // 리프레시 토큰 데이터스토어
    private val loginRequestDataStore: DataStore<LoginRequest>, // 로그인 요청 데이터스토어
    private val sharedPreferences: SharedPreferences // SharedPreferences
) : ViewModel() {

    private val _logoutResponse = MutableStateFlow<LogoutResponse?>(null)
    val logoutResponse: StateFlow<LogoutResponse?> = _logoutResponse

    private val _quitResponse = MutableStateFlow<DeleteResponse?>(null)
    val quitResponse: StateFlow<DeleteResponse?> = _quitResponse

    fun logout() {
        viewModelScope.launch {
            try {
                // 1. 액세스 토큰 가져오기
                val token = accessTokenDataStore.data.map { it.accessToken }.first()

                // 2. 로그아웃 API 호출
                val response = withContext(Dispatchers.IO) {
                    userApi.logout(token)
                }

                // 3. 로그아웃 후 처리
                if (response.result.code == 200) {
                    // 4. 데이터 초기화 (AccessToken, RefreshToken, LoginRequest, SharedPreferences 등)
                    clearUserData()
                }
                _logoutResponse.value = response
            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }

    fun quit() {
        viewModelScope.launch {
            try {
                // 1. 액세스 토큰 가져오기
                val token = accessTokenDataStore.data.map { it.accessToken }.first()

                // 2. 회원탈퇴 API 호출
                val response = withContext(Dispatchers.IO) {
                    userApi.deleteMember(token)
                }
                // 3. 로그아웃 후 처리
                if (response.result.code == 200) {
                    // 4. 데이터 초기화 (AccessToken, RefreshToken, LoginRequest, SharedPreferences 등)
                    clearUserData()
                }
                _quitResponse.value = response
            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }

    // 사용자 데이터 초기화 메서드
    suspend fun clearUserData() {
        // AccessToken 초기화
        accessTokenDataStore.updateData { AccessToken("") }

        // RefreshToken 초기화
        refreshTokenDataStore.updateData { RefreshToken("") }

        // LoginRequest 초기화
        loginRequestDataStore.updateData { LoginRequest("", "") }

        // SharedPreferences 초기화 (로그인 정보 삭제)
        sharedPreferences.edit().clear().apply()
    }
}

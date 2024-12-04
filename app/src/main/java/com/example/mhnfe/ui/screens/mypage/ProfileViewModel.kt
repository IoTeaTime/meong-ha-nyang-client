package com.example.mhnfe.ui.screens.mypage

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.api.UserApi
import com.example.mhnfe.data.remote.request.LoginRequest
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.ApiResponse
import com.example.mhnfe.data.remote.response.ChangeNicknameOrGroupNameResponse
import com.example.mhnfe.data.remote.response.ChangePasswordResponse
import com.example.mhnfe.data.remote.response.DeleteResponse
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.data.remote.response.LogoutResponse
import com.example.mhnfe.data.remote.response.RefreshToken
import com.example.mhnfe.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userApi: UserApi,
    private val groupApi: GroupApi,
    private val userRepository: UserRepository,
    private val groupIdDataStore: DataStore<GroupId>,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val refreshTokenDataStore: DataStore<RefreshToken>, // 리프레시 토큰 데이터스토어
    private val loginRequestDataStore: DataStore<LoginRequest>, // 로그인 요청 데이터스토어
    private val sharedPreferences: SharedPreferences // SharedPreferences
) : ViewModel() {

    private val _logoutResponse = MutableStateFlow<LogoutResponse?>(null)
    val logoutResponse: StateFlow<LogoutResponse?> = _logoutResponse

    private val _quitResponse = MutableStateFlow<DeleteResponse?>(null)
    val quitResponse: StateFlow<DeleteResponse?> = _quitResponse

    private val _changeResponse = MutableStateFlow<ChangePasswordResponse?>(null)
    val changeResponse: StateFlow<ChangePasswordResponse?> = _changeResponse

    private val _changeNicknameOrGroupNameResponse = MutableStateFlow<ChangeNicknameOrGroupNameResponse?>(null)
    val changeNicknameOrGroupNameResponse: StateFlow<ChangeNicknameOrGroupNameResponse?> = _changeNicknameOrGroupNameResponse

    private val _exitGroupResponse = MutableStateFlow<Response<ApiResponse>?>(null)
    val exitGroupResponse: StateFlow<Response<ApiResponse>?> = _exitGroupResponse

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

    fun exitGroup() {
        viewModelScope.launch {
            try {
                // 액세스 토큰 가져오기
                val token = accessTokenDataStore.data.map { it.accessToken }.first()

                // 그룹 ID 가져오기
                val groupId = groupIdDataStore.data.map { it.groupId }.first()

                // 그룹 퇴장 API 호출
                val response = withContext(Dispatchers.IO) {
                    groupApi.exitGroup(groupId, token)
                }

                // 성공 시 그룹 ID 초기화
                if (response.isSuccessful) {
                    groupIdDataStore.updateData { GroupId(0) }
                }
                _exitGroupResponse.value = response

            } catch (e: Exception) {
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

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                // 1. 액세스 토큰 가져오기
                val token = accessTokenDataStore.data.map { it.accessToken }.first()

                // 2. 비밀번호 변경 호출
                val response = withContext(Dispatchers.IO) {
                    userRepository.changePassword(token, currentPassword, newPassword)
                }
                _changeResponse.value = response

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

    fun changeNicknameGroupName(nickname: String, groupName: String) {
        viewModelScope.launch {
            try {
                val token = accessTokenDataStore.data.map { it.accessToken }.first()
                val response = withContext(Dispatchers.IO) {
                    var processedNickname : String? = null
                    var processedGroupName : String? = null

                    if (!nickname.isBlank()){
                        processedNickname = nickname
                    }
                    if (!groupName.isBlank()){
                        processedGroupName = groupName
                    }
                    userRepository.changeNicknameOrGroupName(token,processedNickname,processedGroupName)
                }
                _changeNicknameOrGroupNameResponse.value = response

            }catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }

        }
    }
}

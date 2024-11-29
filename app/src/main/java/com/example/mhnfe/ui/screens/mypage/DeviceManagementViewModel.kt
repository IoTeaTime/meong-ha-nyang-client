package com.example.mhnfe.ui.screens.mypage

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import com.example.mhnfe.data.remote.response.Result
import com.example.mhnfe.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class DeviceManagementViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
): ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private fun mapErrorMessage(code: Int, message: String, description: String?): String {
        return when (message) {
            "NOT FOUND" -> "회원 정보를 찾을 수 없습니다. 다시 확인해주세요."
            "BAD REQUEST" -> "비밀번호가 일치하지 않습니다. 다시 입력해주세요."
            else -> "알 수 없는 오류가 발생했습니다.\n에러 코드: $code\n$description"
        }
    }

    fun deleteDevice(cctvId: Long) {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response: Response<DeleteDeviceResponse>?
            response = deviceRepository.deleteDevice(token, cctvId)
            if(!response.isSuccessful) {
                _errorMessage.value = "기기 삭제 실패 " + response.code()
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}
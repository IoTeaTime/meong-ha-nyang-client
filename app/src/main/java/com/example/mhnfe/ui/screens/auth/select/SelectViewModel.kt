package com.example.mhnfe.ui.screens.auth.select

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.CreateGroupRequest
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SelectViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupApi: GroupApi,
    private val thingId: String,
    private val accessTokenDataStore: DataStore<AccessToken>
) : ViewModel() {

    private val _groupState = MutableStateFlow<Group?>(null)
    val groupState = _groupState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _navigateNext = MutableStateFlow(false)
    val navigateNext = _navigateNext.asStateFlow()

    fun createGroup() {
        Log.d("SelectViewModel", "createGroup 함수 시작")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("SelectViewModel", "토큰 가져오기 시작")
                val token = accessTokenDataStore.data.map { it.accessToken }.first()

                Log.d("SelectViewModel", "토큰: $token")
                val request = CreateGroupRequest(thingId = thingId)
                Log.d("SelectViewModel", "API 호출 시작 - Request: $request")
                val response = withContext(Dispatchers.IO) {
                    groupApi.createGroup(token, request)

                }
                Log.d("SelectViewModel", "API 응답 성공: $response")

                val group = groupRepository.getGroup(response)
                _groupState.value = group
                _navigateNext.value = true

            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    is HttpException -> {
                        Log.e("SelectViewModel", "HTTP 에러: ${e.code()}")
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.e("SelectViewModel", "에러 응답: $errorBody")
                        if (e.code() == 400) {
                            if (errorBody?.contains("그룹이 이미 존재합니다") == true) {
                                _navigateNext.value = true
                            } else {
                                _error.value = errorBody ?: "서버 오류가 발생했습니다"
                            }
                        }
                    }
                    else -> {
                        Log.e("SelectViewModel", "기타 에러 발생", e)
                        _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
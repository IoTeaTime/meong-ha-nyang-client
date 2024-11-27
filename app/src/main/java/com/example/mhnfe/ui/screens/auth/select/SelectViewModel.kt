package com.example.mhnfe.ui.screens.auth.select

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.api.GroupApi
import com.example.mhnfe.data.remote.response.CreateGroupRequest
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val sharedPreferences: SharedPreferences
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val jwtToken = sharedPreferences.getString("jwt_token", null)
                    ?.takeIf { it.isNotEmpty() }
                    ?: throw IllegalStateException("JWT token is not available")

                val request = CreateGroupRequest(thingId = thingId)
                val response = withContext(Dispatchers.IO) {
                    groupApi.createGroup(jwtToken, request)
                }

                val group = groupRepository.getGroup(response)
                _groupState.value = group
                _navigateNext.value = true

            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    is HttpException -> {
                        if (e.code() == 400) {
                            val errorBody = e.response()?.errorBody()?.string()
                            if (errorBody?.contains("그룹이 이미 존재합니다") == true) {
                                // 그룹이 이미 존재하는 경우도 다음 화면으로 이동
                                _navigateNext.value = true
                            }
                        }
                    }
                    else -> {
                        Log.e("SelectViewModel", "Error creating group", e)
                        _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
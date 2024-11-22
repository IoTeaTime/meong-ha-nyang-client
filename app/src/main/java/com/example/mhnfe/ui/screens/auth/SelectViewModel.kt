package com.example.mhnfe.ui.screens.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.api.GroupApi
import com.example.mhnfe.data.model.CreateGroupRequest
import com.example.mhnfe.data.model.Group
import com.example.mhnfe.data.model.GroupResponse
import com.example.mhnfe.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SelectViewModel @Inject constructor(
    private val repository: GroupRepository,
    private val groupApi: GroupApi,
    private val thingId: String,
    private val sharedPreferences: SharedPreferences,
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _groupState = MutableStateFlow<Group?>(null)
    val groupState = _groupState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        viewModelScope.launch {
            groupRepository.getGroupResponse().collect { group ->
                _groupState.value = group
            }
        }
    }


    fun createGroup() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val jwtToken = sharedPreferences.getString("jwt_token", null)
                Log.d("SelectViewModel", "JWT Token: $jwtToken")

                if (jwtToken.isNullOrEmpty()) {
                    _error.value = "JWT token is not available"
                    return@launch
                }

                val authToken = "$jwtToken"
                Log.d("token", "토큰 값: $authToken")

                val request = CreateGroupRequest(thingId = thingId)
                val response = groupApi.createGroup(authToken, request)
                groupRepository.saveGroupResponse(response)
                Log.d("SelectViewModel", "Response received: $response")

                repository.saveGroupResponse(response)
                _groupState.value = response.body
                _error.value = null
            }   catch (e: Exception) {
                when (e) {
                    is HttpException -> {
                        Log.e("SelectViewModel", "HTTP ${e.code()}: ${e.message()}")
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            Log.e("SelectViewModel", "Error body: $errorBody")
                        } catch (e2: Exception) {
                            Log.e("SelectViewModel", "Error reading error body", e2)
                        }
                    }
                    else -> Log.e("SelectViewModel", "Error creating group", e)
                }
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
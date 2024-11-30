package com.example.mhnfe.ui.screens.auth.select

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.response.Group
import com.example.mhnfe.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SelectViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
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
                groupRepository.createGroup()
                    .onSuccess { group ->
                        _groupState.value = group
                        _navigateNext.value = true
                    }
                    .onFailure { e ->
                        when (e) {
                            is HttpException -> {
                                if (e.code() == 400) {
                                    val errorBody = e.response()?.errorBody()?.string()
                                    Log.d("SelectViewModel", "HTTP 400 에러 응답: $errorBody")
                                    if (errorBody?.contains("그룹이 이미 존재합니다") == true) {
                                        _navigateNext.value = true
                                    } else {
                                        _error.value = errorBody ?: "서버 오류가 발생했습니다"
                                    }
                                }
                            }
                            is CancellationException -> throw e
                            else -> {
                                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
                            }
                        }
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
package com.example.mhnfe.ui.screens.monitoring.group

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.request.GroupInfo
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupIdDataStore: DataStore<GroupId>,
) : ViewModel() {
    private val _groupInfo = MutableStateFlow<GroupInfo?>(null)
    val groupInfo = _groupInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun fetchGroupInfo(groupInfoCallback: (GroupInfo) -> Unit ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            groupRepository.getGroupInfo().fold(
                onSuccess = { response ->
                    Log.d("GroupViewModel", "응답 바디: ${response.body}")
                    Log.d("GroupViewModel", "응답 result: ${response.result}")
                    _groupInfo.value = response.body
                    groupIdDataStore.updateData { currentGroupId ->
                        currentGroupId.copy(groupId = response.body.groupId)
                    }
                    groupInfoCallback(response.body)
                },
                onFailure = { e ->
                    when (e) {
                        is HttpException -> {
                            when (e.code()) {
                                500 -> {
                                    Log.e("GroupViewModel", "Server error response: ${e.response()?.errorBody()?.string()}")
                                    _error.value = "서버 내부 오류가 발생했습니다."
                                }
                                401 -> _error.value = "인증에 실패했습니다."
                                404 -> _error.value = "해당 그룹을 찾을 수 없습니다."
                                else -> _error.value = "오류가 발생했습니다: ${e.code()}"
                            }
                        }
                        else -> {
                            Log.e("GroupViewModel", "Unexpected error : ${e.message}")

                            _error.value = "예기치 못한 오류가 발생했습니다: ${e.message}"
                        }
                    }
                }
            )
            _isLoading.value = false
        }
    }
}
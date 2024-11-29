package com.example.mhnfe.ui.screens.mypage

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.ChangeCctvNicknameResponse
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import com.example.mhnfe.data.remote.response.GroupId
import com.example.mhnfe.data.remote.response.GroupMember
import com.example.mhnfe.data.remote.response.GroupMemberInfo
import com.example.mhnfe.data.remote.response.GroupMemberInfoResponse
import com.example.mhnfe.data.remote.response.GroupMemberResponse
import com.example.mhnfe.domain.repository.DeviceRepository
import com.example.mhnfe.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class DeviceManagementViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val deviceRepository: DeviceRepository,
    private val accessTokenDataStore: DataStore<AccessToken>,
    private val groupIdDataStore: DataStore<GroupId>
): ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _groupMemberInfo = MutableStateFlow<GroupMember?>(null)
    val groupMemberInfo: StateFlow<GroupMember?> = _groupMemberInfo
    private val _groupMemberInfoList = MutableStateFlow<List<GroupMemberInfo>?>(null)
    val groupMemberInfoList: StateFlow<List<GroupMemberInfo>?> = _groupMemberInfoList

    fun deleteDevice(cctvId: Long) {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response: Response<DeleteDeviceResponse>?
            response = deviceRepository.deleteDevice(token, cctvId)
            if(!response.isSuccessful) {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                val errorBody = Json.decodeFromString<DeleteDeviceResponse>(jsonObject.toString())

                _errorMessage.value = errorBody.result.description
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }

    fun changeCctvName(cctvId: Long, cctvName: String) {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response: Response<ChangeCctvNicknameResponse>?
            response = deviceRepository.changeCctvName(token, cctvId, cctvName)
            if(!response.isSuccessful) {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                val errorBody = Json.decodeFromString<ChangeCctvNicknameResponse>(jsonObject.toString())
                _errorMessage.value = errorBody.result.description
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    fun getGroupMemberList() {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val groupId = groupIdDataStore.data.map { it.groupId }.first()
            val response: Response<GroupMemberInfoResponse> =
                groupRepository.getGroupMemberList(groupId, token)
            if(response.isSuccessful) {
                _groupMemberInfoList.value = response.body()?.body?.member
            } else {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                val errorBody = Json.decodeFromString<DeleteDeviceResponse>(jsonObject.toString())

                _errorMessage.value = errorBody.result.description
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }

    fun getGroupMemberInfo() {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response: Response<GroupMemberResponse> = groupRepository.getGroupMember(token)
            if(response.isSuccessful) {
                _groupMemberInfo.value = response.body()?.body
            } else {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                val errorBody = Json.decodeFromString<DeleteDeviceResponse>(jsonObject.toString())

                _errorMessage.value = errorBody.result.description
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }
}
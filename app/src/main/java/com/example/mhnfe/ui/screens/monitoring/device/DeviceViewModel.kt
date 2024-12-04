package com.example.mhnfe.ui.screens.monitoring.device

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.remote.request.CctvInfoResponse
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import com.example.mhnfe.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val accessTokenDataStore: DataStore<AccessToken>
): ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 단일 CCTV 정보 조회 API
    fun getCctvInfo(cctvId: Long, onCctvInfoFetched: (thingId: String) -> Unit) {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response: Response<CctvInfoResponse> = deviceRepository.getCctvInfo(token, cctvId)
            if(response.isSuccessful) {
                response.body()?.body?.thingId?.let(onCctvInfoFetched)
            } else {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                val errorBody = Json.decodeFromString<DeleteDeviceResponse>(jsonObject.toString())

                _errorMessage.value = errorBody.result.description
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }
}
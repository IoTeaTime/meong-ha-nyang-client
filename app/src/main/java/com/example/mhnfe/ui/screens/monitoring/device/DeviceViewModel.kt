package com.example.mhnfe.ui.screens.monitoring.device

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mhnfe.data.model.CCTV
import com.example.mhnfe.data.remote.request.CctvInfo
import com.example.mhnfe.data.remote.request.CctvInfoResponse
import com.example.mhnfe.data.remote.response.AccessToken
import com.example.mhnfe.data.remote.response.DeleteDeviceResponse
import com.example.mhnfe.domain.repository.DeviceRepository
import com.example.mhnfe.ui.screens.monitoring.group.toCCTV
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _cctv = MutableStateFlow<CCTV?>(null)
    val cctv: StateFlow<CCTV?> = _cctv.asStateFlow()

    // 단일 CCTV 정보 조회 API
    fun getCctvInfo(cctvId: Long, onCctvInfoFetched: (cctvInfo: CctvInfo) -> Unit) {
        viewModelScope.launch {
            val token = accessTokenDataStore.data.map { it.accessToken }.first()
            val response: Response<CctvInfoResponse> = deviceRepository.getCctvInfo(token, cctvId)
            if(response.isSuccessful) {
                _cctv.value = response.body()?.body?.toCCTV()
                response.body()?.body?.let { onCctvInfoFetched(it) }
            } else {
                val jsonObject = JSONObject(response.errorBody()!!.string())
                val errorBody = Json.decodeFromString<DeleteDeviceResponse>(jsonObject.toString())

                _errorMessage.value = errorBody.result.description
                Log.e(TAG,"Error: " + _errorMessage.value)
            }
        }
    }
}
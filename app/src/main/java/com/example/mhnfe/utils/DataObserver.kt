package com.example.mhnfe.utils

import DeviceUtils
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataObserver(private val context: Context) {
    private val batteryLevelFlow = MutableStateFlow(DeviceUtils.getBatteryLevel(context))
    private val networkStatusFlow = MutableStateFlow( -1)
    private val kvsChannelActiveFlow = MutableStateFlow(false)
    private val isBackCameraFlow = MutableStateFlow(false)

    val batteryLevel: StateFlow<Int> get() = batteryLevelFlow
    val networkStatus: StateFlow<Int>get() = networkStatusFlow
    val kvsChannelActive: StateFlow<Boolean> get() = kvsChannelActiveFlow

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun startObserving() {
        CoroutineScope(Dispatchers.IO).launch {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)


            while (true) {
                val newBatteryLevel = DeviceUtils.getBatteryLevel(context)
                if (newBatteryLevel != batteryLevelFlow.value) {
                    batteryLevelFlow.value = newBatteryLevel
                }
                delay(5000) // 5초마다 업데이트
            }
        }
    }

    // 네트워크 상태 변경 감지 콜백
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiInfo = networkCapabilities.transportInfo as? WifiInfo
                wifiInfo?.let {
                    val rssi = it.rssi
                    val signalStrength = getSignalLevel(rssi)
                    networkStatusFlow.value = signalStrength
                }
            }
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            // 네트워크가 끊어졌을 때
            networkStatusFlow.value = -1
        }
    }

    private fun getSignalLevel(rssi: Int): Int {
        // Wi-Fi 신호 강도를 0~4로 정규화
        return when {
            rssi >= -50 -> 4 // Excellent
            rssi >= -60 -> 3 // Good
            rssi >= -70 -> 2 // Fair
            rssi >= -80 -> 1 // Weak
            else -> 0 // No Signal
        }
    }

    fun updateKvsChannelState(isActive: Boolean) {
        kvsChannelActiveFlow.value = isActive
    }
}

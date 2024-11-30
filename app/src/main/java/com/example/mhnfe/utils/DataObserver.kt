package com.example.mhnfe.utils

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// DataObserver: 실시간 데이터 감지를 위한 클래스
class DataObserver(private val context: Context) {
    private val batteryLevelFlow = MutableStateFlow(DeviceUtils.getBatteryLevel(context))
    private val networkStatusFlow = MutableStateFlow(getNetworkStatus())
    private val kvsChannelActiveFlow = MutableStateFlow(false)

    val batteryLevel: StateFlow<Int> get() = batteryLevelFlow
    val networkStatus: StateFlow<Pair<String, Int>> get() = networkStatusFlow
    val kvsChannelActive: StateFlow<Boolean> get() = kvsChannelActiveFlow

    fun startObserving() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val newBatteryLevel = DeviceUtils.getBatteryLevel(context)
                if (newBatteryLevel != batteryLevelFlow.value) {
                    batteryLevelFlow.value = newBatteryLevel
                }

                val newNetworkStatus = getNetworkStatus()
                if (newNetworkStatus != networkStatusFlow.value) {
                    networkStatusFlow.value = newNetworkStatus
                }

                delay(5000) // 5초마다 업데이트
            }
        }
    }

    private fun getNetworkStatus(): Pair<String, Int> {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectionInfo = wifiManager.connectionInfo
        return if (connectionInfo != null) {
            val ssid = connectionInfo.ssid?.removeSurrounding("\"") ?: "Unknown"
            val signalStrength = wifiManager.calculateSignalLevel(connectionInfo.rssi)
            Pair(ssid, signalStrength)
        } else {
            Pair("No Wi-Fi", -1)
        }
    }

    fun updateKvsChannelState(isActive: Boolean) {
        kvsChannelActiveFlow.value = isActive
    }
}

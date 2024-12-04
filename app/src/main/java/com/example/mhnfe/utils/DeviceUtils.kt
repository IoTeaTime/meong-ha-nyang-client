
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.os.BatteryManager
import android.os.Build
import org.json.JSONObject

object DeviceUtils {
    fun getShadowPayload(context: Context): String {
        val batteryLevel = getBatteryLevel(context)
        val kvsChannelActive = true
        val kvsChannelDeleteRequested = false
        val networkStatus = getWifiInfo(context)

        return """
        {
            "state": {
                "reported": {
                    "batteryLevel": $batteryLevel,
                    "kvsChannelActive": $kvsChannelActive,
                    "kvsChannelDeleteRequested": $kvsChannelDeleteRequested,
                    "networkStatus": $networkStatus
                }
            }
        }
        """.trimIndent()
    }

    fun getPublishPayload(context: Context, groupMessage: JSONObject): String {
        val batteryLevel = getBatteryLevel(context)
        val availableMemory = getAvailableMemory(context)
        val (deviceModel, osVersion) = getDeviceInfo()
        val appVersion = getAppVersion(context)
        val networkStatus = getWifiInfo(context)
        val timestamp = groupMessage.optLong("timestamp", System.currentTimeMillis() / 1000)

        return """
        {
            "batteryLevel": $batteryLevel,
            "availableMemory": $availableMemory,
            "deviceModel": "$deviceModel",
            "osVersion": "$osVersion",
            "appVersion": "$appVersion",
            "networkStatus": "$networkStatus",
            "timestamp": $timestamp
        }
        """.trimIndent()
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryIntent =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            (level * 100) / scale
        } else {
            -1 // 오류 발생 시
        }
    }

    private fun getAvailableMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024) // MB 단위
    }

    private fun getDeviceInfo(): Pair<String, String> {
        val deviceModel = Build.MODEL // 기종
        val osVersion = Build.VERSION.RELEASE // OS 버전
        return Pair(deviceModel, osVersion)
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getWifiInfo(context: Context): JSONObject {
        val wifiInfoJson = JSONObject()

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities: NetworkCapabilities? = cm.getNetworkCapabilities(cm.activeNetwork)

        if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            val wifiInfo = networkCapabilities.transportInfo as? WifiInfo
            if (wifiInfo != null) {
                val rssi = wifiInfo.rssi
                val signalStrength = getSignalLevel(rssi)
                wifiInfoJson.put("SignalStrength", signalStrength)
            } else {
                wifiInfoJson.put("Error", "No Wi-Fi info available")
            }
        } else {
            wifiInfoJson.put("Error", "Not connected to Wi-Fi")
        }

        return wifiInfoJson
    }

    private fun getSignalLevel(rssi: Int): Int {
        return when {
            rssi >= -50 -> 4 // Excellent
            rssi >= -60 -> 3 // Good
            rssi >= -70 -> 2 // Fair
            rssi >= -80 -> 1 // Weak
            else -> 0 // Poor or no signal
        }
    }

    fun getBatteryPayload(newBatteryLevel: Int): String {
        return """
        {
            "state": {
                "reported": {
                    "batteryLevel": $newBatteryLevel
                }
            }
        }
        """.trimIndent()
    }

    fun getNetworkPayload(networkStatus: Int): String {
        return """
        {
            "state": {
                "reported": {
                    "networkStatus": {
                        "SignalStrength": $networkStatus
                    }
                }
            }
        }
        """.trimIndent()
    }

    fun getKvsChannelPayload(isActive: Boolean): String {
        return """
        {
            "state": {
                "reported": {
                    "kvsChannelActive": $isActive
                }
            }
        }
        """.trimIndent()
    }
}

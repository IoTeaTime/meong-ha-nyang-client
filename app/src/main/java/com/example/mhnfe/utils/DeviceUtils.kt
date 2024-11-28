import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import org.json.JSONObject

object DeviceUtils {
    // Shadow용 Payload 생성
    fun getShadowPayload(context: Context): String {
        val kvsChannelActive = true
        val kvsChannelDeleteRequested = false
        val networkStatus = getNetworkStatus(context)

        return """
        {
            "state": {
                "reported": {
                    "kvsChannelActive": $kvsChannelActive,
                    "kvsChannelDeleteRequested": $kvsChannelDeleteRequested,
                    "networkStatus": "$networkStatus"
                }
            }
        }
        """.trimIndent()
    }

    fun getPublishPayload(context: Context, groupMessage: JSONObject): String {
        val batteryLevel = getBatteryLevel(context)
        val availableMemory = getAvailableMemory(context)

        // 그룹 정보에서 필요한 데이터 추출
        val groupInfo = groupMessage.optString("groupInfo", "Unknown Group")
        val timestamp = groupMessage.optLong("timestamp", System.currentTimeMillis() / 1000)
        val metadata = groupMessage.optJSONObject("metadata")
        val location = metadata?.optString("location", "Unknown Location")
        val type = metadata?.optString("type", "Unknown Type")

        // 페이로드 생성
        return """
        {
            "batteryLevel": $batteryLevel,
            "availableMemory": $availableMemory,
            "status": "online",
            "group": "$groupInfo",
            "timestamp": $timestamp,
            "location": "$location",
            "type": "$type"
        }
        """.trimIndent()
    }
    // 배터리 수준 가져오기
    private fun getBatteryLevel(context: Context): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            (level * 100) / scale
        } else {
            -1 // 오류 발생 시
        }
    }

    // 사용 가능한 메모리 가져오기
    private fun getAvailableMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024) // MB 단위로 반환
    }

    // 기종 및 OS 버전 가져오기
    private fun getDeviceInfo(): Pair<String, String> {
        val deviceModel = Build.MODEL // 기종 (예: Galaxy S21)
        val osVersion = Build.VERSION.RELEASE // OS 버전 (예: 13)
        return Pair(deviceModel, osVersion)
    }

    // 앱 버전 가져오기
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }



    // 네트워크 상태 가져오기
    private fun getNetworkStatus(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return "No Connection"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "No Connection"

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
    }
}

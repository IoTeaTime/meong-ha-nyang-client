package com.example.mhnfe.mqtt

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.example.mhnfe.mqtt.shadow.delta.ShadowDeltaMsg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyStore
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class MqttViewModel @Inject constructor(
    private val thingId: String,
    private val iotClientHelper: IoTClientHelper,
    private val mqttHelper: MqttManagerHelper
): ViewModel() {
    private var awsMqttManager: AWSIotMqttManager = mqttHelper.createMqttManager()
    private var keyStore: KeyStore? = null
    private val tag = "MqttUtils"

    @SuppressLint("HardwareIds")
    fun initialize(context: Context) {
        val keyStoreFile = File("${context.filesDir}/keystore.bks")

        thread {
            try {
                if (keyStoreFile.exists()) {
                    // 기존 KeyStore를 사용하는 경우
                    initializeWithExistingKeyStore(context)
                } else {
                    // 새로운 KeyStore를 생성해야 하는 경우
                    initializeWithNewKeyStore(context)
                }
                // MQTT Manager 연결
                connectToMqttManager()
            } catch (e: Exception) {
                Log.e(tag, "Initialization error: ${e.message}", e)
            }

            // MQTT Manager 연결
            connectToMqttManager() // 연결 완료까지 대기
        } catch (e: Exception) {
            Log.e(tag, "Initialization error: ${e.message}", e)
            throw e // 오류를 상위로 전달
        }

        return@withContext androidId
    }



    private fun initializeWithExistingKeyStore(context: Context) {
        try {
            keyStore = mqttHelper.getKeyStore(context)
            Log.d(tag, "KeyStore found and MQTT Manager initialized.")
        } catch (e: Exception) {
            Log.e(tag, "KeyStore access error: ${e.message}", e)
            initializeWithNewKeyStore(context)
        }
    }

    private fun initializeWithNewKeyStore(context: Context) {
        try {
            val awsKeyAndCert = iotClientHelper.getKeyAndCert()
            awsKeyAndCert.let {
                // KeyStore 생성 및 저장
                iotClientHelper.registerDevice(context, it)
                mqttHelper.createKeyStore(context, it)

                keyStore = mqttHelper.getKeyStore(context)
                Log.d(tag, "New KeyStore created and MQTT Manager initialized.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error while creating new KeyStore: ${e.message}", e)
        }
    }



    private suspend fun connectToMqttManager() = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                awsMqttManager?.connect(keyStore) { status, throwable ->
                    if (throwable != null) {
                        Log.e(tag, "Connection error: ${throwable.message}", throwable)
                        // 예외를 전달하여 코루틴 재개
                        if (continuation.isActive) {
                            continuation.resumeWithException(throwable)
                        }
                    } else {
                        Log.d(tag, "MQTT Connection Status: $status")
                        if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                            if (continuation.isActive) {
                                continuation.resume(Unit) // 연결 완료 시 코루틴 재개
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "MQTT Connection failed: ${e.message}", e)
            throw e
        }




    fun disconnectMqttManager() {
        awsMqttManager.disconnect()
        Log.d(tag, "MQTT Manager Disconnected")
    }

    // MQTT Publish 기능
    fun publish(topic: String, payload: String) {
        awsMqttManager.publishString(payload, topic, AWSIotMqttQos.QOS0)
    }

    // MQTT Subscribe 기능
    fun subscribe(topic: String, onMessageReceived: (String, String) -> Unit) {
        awsMqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0) { receivedTopic, message ->
            onMessageReceived(receivedTopic, message.toString(Charsets.UTF_8))
        }
    }

    fun viewerInitialSubscribe(thingList: List<String>, groupId: Int) {
        try {
            // Group 관련 토픽 생성
            val groupTopic = "/mhn/command/device/info/groups/$groupId"
            val topicsToSubscribe = mutableListOf(groupTopic)

            // Thing 리스트의 각 Thing에 대해 토픽 생성 및 추가
            thingList.forEach { thingId ->
                val thingTopic = "/mhn/command/device/info/things/$thingId"
                topicsToSubscribe.add(thingTopic)
            }

            // 각 Topic에 대해 구독 설정
            topicsToSubscribe.forEach { topic ->
                subscribe(topic) { receivedTopic, message ->
                    Log.d(tag, "Message received on Topic: $receivedTopic, Payload: $message")
                    // 메시지 내용을 처리
                    handleTopicMessage(receivedTopic, message)
                }
            }
            Log.d(tag, "Viewer 초기 구독 완료: Topics=${topicsToSubscribe.joinToString(", ")}")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 구독 실패: ${e.message}", e)
        }
    }

    private fun handleTopicMessage(receivedTopic: String, message: String) {
        try {
            // JSON 메시지 파싱 (예시)
            val jsonMessage = JSONObject(message)

            when {
                receivedTopic.contains("groups") -> {
                    // 그룹 관련 메시지 처리
                    val groupInfo = jsonMessage.optString("groupInfo", "N/A")
                    Log.d(tag, "Received group info: $groupInfo")
                }
                receivedTopic.contains("things") -> {
                    // Thing 관련 메시지 처리
                    val thingId = receivedTopic.substringAfterLast("/")
                    val status = jsonMessage.optString("status", "unknown")
                    val batteryLevel = jsonMessage.optInt("batteryLevel", -1)
                    Log.d(tag, "Received status for Thing $thingId: Status=$status, Battery=$batteryLevel")
                }
                else -> {
                    // 기타 메시지 처리
                    Log.w(tag, "Unhandled topic: $receivedTopic")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to process message for topic: $receivedTopic, Error: ${e.message}", e)
        }
    }



    fun createShadowWithSubscribe(thingId: String, context: Context) {
        try {
            // Shadow Update Topic
            val topic = "\$aws/things/${thingId}/shadow/update"

            // Shadow Payload (하드코딩된 샘플 데이터)
            val batteryLevel = getBatteryLevel(context)
            val availableMemory = getAvailableMemory(context)
            val kvsChannelActive = true
            val kvsChannelDeleteRequested = false

            val payload = """
            {
                "state": {
                    "reported": {
                        "kvsChannelActive": $kvsChannelActive,
                        "batteryLevel": $batteryLevel,
                        "availableMemory": $availableMemory,
                        "kvsChannelDeleteRequested": $kvsChannelDeleteRequested
                    }
                }
            }
        """.trimIndent()

            // Shadow 관련 Topic 구독
            val shadowTopics = listOf(
                "\$aws/things/${thingId}/shadow/update/delta",
                "\$aws/things/${thingId}/shadow/update/accepted",
                "\$aws/things/${thingId}/shadow/update/rejected",
                "\$aws/things/${thingId}/shadow/update/documents",
                "\$aws/things/${thingId}/shadow/get/accepted",
                "\$aws/things/${thingId}/shadow/get/rejected"
            )

            // 각 Topic에 대해 구독 설정
            shadowTopics.forEach { shadowTopic ->
                subscribe(shadowTopic) { receivedTopic, message ->
                    Log.d(tag, "Message received on Topic: $receivedTopic, Payload: $message")
                    // 추가 로직: 메시지 내용을 처리 (예: Shadow 업데이트, UI 반영 등)
                    handleShadowMessage(receivedTopic, message)
                }
            }

            // Shadow 상태 업데이트 메시지 발행
            publish(topic, payload)
            Log.d(tag, "MQTT 메시지 발행 성공: Topic=$topic, Payload=$payload")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 메시지 발행 실패: ${e.message}", e)
        }
    }

    /**
     * Shadow 메시지 처리 함수
     * @param topic - 메시지가 발행된 Topic
     * @param message - 수신된 메시지 내용
     */
    private fun handleShadowMessage(topic: String, message: String) {
        try {
            val jsonObject: ShadowDeltaMsg = Json.decodeFromString<ShadowDeltaMsg>(message)
            Log.d(tag, "처리된 메시지: $jsonObject")
            // 메시지 내용에 따라 적절한 처리를 구현
            when {
                topic.contains("delta") -> {
                    // Delta 메시지 처리 로직
                    Log.d(tag, "Delta 메시지 수신: $jsonObject")

                    // kvsChannelDeleteRequested가 true라면
                    if(jsonObject.state.delta.kvsChannelDeleteRequested) {
                        // IoT 디바이스 삭제
                        iotClientHelper.deleteDevice()
                        Log.d(tag, "IoT 디바이스 삭제 성공")
                    } else {
                        // 그렇지 않다면 내 정보를 update

                    }
                }

                topic.contains("accepted") -> {
                    // Update/Accepted 메시지 처리 로직
                    Log.d(tag, "Accepted 메시지 수신: $jsonObject")
                }

                topic.contains("rejected") -> {
                    // Update/Rejected 메시지 처리 로직
                    Log.e(tag, "Rejected 메시지 수신: $jsonObject")
                }

                topic.contains("documents") -> {
                    // Update/Documents 메시지 처리 로직
                    Log.d(tag, "Documents 메시지 수신: $jsonObject")
                }

                else -> {
                    Log.w(tag, "알 수 없는 Shadow Topic 수신: $topic")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Shadow 메시지 처리 실패: ${e.message}", e)
        }
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

    fun getAvailableMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024) // MB 단위
    }
}

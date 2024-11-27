package com.example.mhnfe.domain.mqtt

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.example.mhnfe.domain.mqtt.shadow.delta.ShadowDeltaMsg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyStore
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class MqttViewModel @Inject constructor(
    private val thingId: String,
    private val iotClientHelper: IoTClientHelper,
    private val mqttHelper: MqttManagerHelper
) : ViewModel() {
    private var awsMqttManager: AWSIotMqttManager = mqttHelper.createMqttManager()
    private var keyStore: KeyStore? = null
    private val tag = "MqttUtils"


    @SuppressLint("HardwareIds")
    suspend fun initialize(context: Context): Boolean = withContext(Dispatchers.IO) {
        val keyStoreFile = File("${context.filesDir}/keystore.bks")
        return@withContext try {
            if (keyStoreFile.exists()) {
                // 기존 KeyStore를 사용하는 경우
                initializeWithExistingKeyStore(context)
            } else {
                // 새로운 KeyStore를 생성해야 하는 경우
                initializeWithNewKeyStore(context)
            }
            // MQTT Manager 연결
            val isConnected = connectToMqttManager()
            if (isConnected) {
                Log.d(tag, "MQTT 연결 성공")
            } else {
                Log.e(tag, "MQTT 연결 실패")
            }
            isConnected // 연결 성공 여부 반환
        } catch (e: Exception) {
            Log.e(tag, "Initialization error: ${e.message}", e)
            false // 실패 시 false 반환
        }
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


    private suspend fun connectToMqttManager(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            suspendCancellableCoroutine<Boolean> { continuation ->
                awsMqttManager?.connect(keyStore) { status, throwable ->
                    if (throwable != null) {
                        Log.e(tag, "Connection error: ${throwable.message}", throwable)
                        // 예외 전달하여 코루틴 재개
                        if (continuation.isActive) {
                            continuation.resumeWithException(throwable)
                        }
                    } else {
                        if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                            if (continuation.isActive) {
                                continuation.resume(true) // 성공 시 true 반환
                            }
                        } else if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost) {
                            if (continuation.isActive) {
                                continuation.resume(false) // 연결 끊김 시 false 반환
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "MQTT Connection failed: ${e.message}", e)
            false // 실패 시 false 반환
        }
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

    fun viewerInitialSubscribe(context: Context, thingList: List<String>) {
        try {
            val topicsToSubscribe: MutableList<String> = mutableListOf()

            // Thing 리스트의 각 Thing에 대해 토픽 생성 및 추가
            thingList.forEach { subthingId ->
                val thingTopic = "/mhn/command/device/info/things/$subthingId"
                topicsToSubscribe.add(thingTopic)
            }

            // 각 Topic에 대해 구독 설정
            topicsToSubscribe.forEach { topic ->
                subscribe(topic) { receivedTopic, message ->
                    Log.d(tag, "Message received on Topic: $receivedTopic, Payload: $message")
                    // 메시지 내용을 처리
                    handleTopicMessage(receivedTopic, message, context)
                }
            }
            Log.d(tag, "Viewer 초기 구독 완료: Topics=${topicsToSubscribe.joinToString(", ")}")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 구독 실패: ${e.message}", e)
        }
    }

    fun createShadowWithSubscribe(context: Context, groupId: Int) {
        try {
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

            // Pub-Topic
            val topic = "\$aws/things/${thingId}/shadow/update"
            val payload = DeviceUtils.getShadowPayload(context)

            // Shadow 상태 업데이트 메시지 발행
            publish(topic, payload)

            // 그룹 관련 Sub-Topic 구독
            val groupTopic = "/mhn/command/device/info/groups/$groupId"
            subscribe(groupTopic) { receivedTopic, message ->
                Log.d(tag, "Message received on Group Topic: $receivedTopic, Payload: $message")
                // 메시지 내용 처리
                handleTopicMessage(receivedTopic, message, context)
            }

            Log.d(tag, "MQTT 메시지 발행 성공: Topic=$topic, Payload=$payload")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 메시지 발행 실패: ${e.message}", e)
        }
    }

    // Subscribe Topic 처리 함수
    private fun handleTopicMessage(receivedTopic: String, message: String, context: Context) {
        try {
            // JSON 메시지를 JSONObject로 파싱
            val jsonMessage = JSONObject(message)
            Log.d(tag, "Received JSON message: $jsonMessage")

            // Topic 기반 처리
            when {
                receivedTopic.contains("groups") -> {
                    val groupInfo = jsonMessage.optString("groupInfo", "No Group Info")
                    Log.d(tag, "Group Info: $groupInfo")

                    // DeviceUtils로 Payload 생성
                    val payload = DeviceUtils.getPublishPayload(context)

                    // Thing Topic으로 Publish
                    val thingTopic = "/mhn/command/device/info/things/$thingId"
                    publish(thingTopic, payload)
                    Log.d(tag, "Published Device Info to Thing Topic: Topic=$thingTopic, Payload=$payload")
                }

                receivedTopic.contains("things") -> {
                    val thingId = receivedTopic.substringAfterLast("/")
                    val status = jsonMessage.optString("status", "unknown")
                    val batteryLevel = jsonMessage.optInt("batteryLevel", -1)
                    Log.d(tag, "Thing ID: $thingId, Status: $status, Battery Level: $batteryLevel")
                }

                else -> {
                    Log.d(tag, "Unhandled Topic: $receivedTopic, Message: $jsonMessage")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to process message on Topic: $receivedTopic, Error: ${e.message}", e)
        }
    }

    // Shadow 메시지 처리 함수
    private fun handleShadowMessage(topic: String, message: String) {
        try {
            val jsonObject: ShadowDeltaMsg = Json.decodeFromString(message)
            Log.d(tag, "처리된 Shadow 메시지: $jsonObject")

            when {
                topic.contains("delta") -> {
                    Log.d(tag, "Delta 메시지 수신: $jsonObject")
                    if (jsonObject.state.delta.kvsChannelDeleteRequested) {
                        iotClientHelper.deleteDevice()
                        Log.d(tag, "IoT 디바이스 삭제 성공")
                    } else {
                        Log.d(tag, "Delta 처리 완료: $jsonObject")
                    }
                }

                topic.contains("accepted") -> {
                    Log.d(tag, "Accepted 메시지 수신: $jsonObject")
                }

                topic.contains("rejected") -> {
                    Log.e(tag, "Rejected 메시지 수신: $jsonObject")
                }

                topic.contains("documents") -> {
                    Log.d(tag, "Documents 메시지 수신: $jsonObject")
                }

                else -> {
                    Log.w(tag, "Unhandled Shadow Topic: $topic")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Shadow 메시지 처리 실패: ${e.message}", e)
        }
    }

    fun publishAIResult(payload: String) {
        try {
            val topic = "/mhn/event/detect/things/$thingId" // MQTT 토픽
            awsMqttManager.publishString(payload, topic, AWSIotMqttQos.QOS0)
            Log.d(tag, "MQTT 이벤트 발행 성공 - Topic: $topic, Payload: $payload")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 이벤트 발행 실패: ${e.message}", e)
        }
    }
}

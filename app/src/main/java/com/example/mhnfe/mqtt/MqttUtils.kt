package com.example.mhnfe.mqtt

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import kotlin.concurrent.thread

object MqttUtils {
    private var awsMqttManager: AWSIotMqttManager? = null
    private var iotClientHelper: IoTClientHelper? = null
    private var mqttHelper: MqttManagerHelper? = null
    private var keyStore: KeyStore? = null
    private const val tag = "MqttUtils"
    private var androidId: String = ""

    @SuppressLint("HardwareIds")
    fun initialize(context: Context) {
        androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        // IoTClientHelper와 MqttManagerHelper 초기화
        if (iotClientHelper == null) {
            iotClientHelper = IoTClientHelper(androidId)
        }
        if (mqttHelper == null) {
            mqttHelper = MqttManagerHelper(androidId)
        }

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
        }
    }

    private fun initializeWithExistingKeyStore(context: Context) {
        try {
            keyStore = mqttHelper?.getKeyStore(context)
            awsMqttManager = mqttHelper?.createMqttManager()
            Log.d(tag, "KeyStore found and MQTT Manager initialized.")
        } catch (e: Exception) {
            Log.e(tag, "KeyStore access error: ${e.message}", e)
            initializeWithNewKeyStore(context)
        }
    }

    private fun initializeWithNewKeyStore(context: Context) {
        try {
            val awsKeyAndCert = iotClientHelper?.getKeyAndCert()
            awsKeyAndCert?.let {
                // KeyStore 생성 및 저장
                iotClientHelper?.registerDevice(context, it)
                mqttHelper?.createKeyStore(context, it)

                keyStore = mqttHelper?.getKeyStore(context)
                awsMqttManager = mqttHelper?.createMqttManager()
                Log.d(tag, "New KeyStore created and MQTT Manager initialized.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error while creating new KeyStore: ${e.message}", e)
        }
    }

    private fun connectToMqttManager() {
        awsMqttManager?.connect(keyStore) { status, throwable ->
            if (throwable != null) {
                Log.e(tag, "Connection error: ${throwable.message}", throwable)
            } else {
                Log.d(tag, "MQTT Connection Status: $status")
                if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    createShadowWithSubscribe(androidId)
                }
            }
        }
    }

    fun disconnectMqttManager() {
        awsMqttManager?.disconnect()
        Log.d(tag, "MQTT Manager Disconnected")
    }

    // MQTT Publish 기능
    fun publish(topic: String, payload: String) {
        awsMqttManager?.publishString(payload, topic, AWSIotMqttQos.QOS0) ?: run {
            Log.e(tag, "MQTT Manager is not initialized")
        }
    }

    // MQTT Subscribe 기능
    fun subscribe(topic: String, onMessageReceived: (String, String) -> Unit) {
        awsMqttManager?.subscribeToTopic(topic, AWSIotMqttQos.QOS0) { receivedTopic, message ->
            onMessageReceived(receivedTopic, message.toString(Charsets.UTF_8))
        } ?: run {
            Log.e(tag, "MQTT Manager is not initialized")
        }
    }

    fun createShadowWithSubscribe(thingId: String) {
        try {
            // Shadow Update Topic
            val topic = "\$aws/things/${thingId}/shadow/update"

            // Shadow Payload (하드코딩된 샘플 데이터)
            val batteryLevel = 91
            val availableMemory = 126
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
                MqttUtils.subscribe(shadowTopic) { receivedTopic, message ->
                    Log.d(tag, "Message received on Topic: $receivedTopic, Payload: $message")
                    // 추가 로직: 메시지 내용을 처리 (예: Shadow 업데이트, UI 반영 등)
                    handleShadowMessage(receivedTopic, message)
                }
            }

            // Shadow 상태 업데이트 메시지 발행
            MqttUtils.publish(topic, payload)
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
            val jsonObject = JSONObject(message)
            Log.d(tag, "처리된 메시지: $jsonObject")
            // 메시지 내용에 따라 적절한 처리를 구현
            when {
                topic.contains("delta") -> {
                    // Delta 메시지 처리 로직
                    Log.d(tag, "Delta 메시지 수신: $jsonObject")
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
}

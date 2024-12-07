package com.example.mhnfe.domain.mqtt

import DeviceUtils
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.example.mhnfe.domain.mqtt.shadow.DeviceInfoShadow
import com.example.mhnfe.domain.mqtt.shadow.ReportedData
import com.example.mhnfe.domain.mqtt.shadow.delta.ShadowDeltaMsg
import com.example.mhnfe.domain.mqtt.topic.ReportedThingInfo
import com.example.mhnfe.utils.DataObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyStore
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class MqttViewModel @Inject constructor(
    private val thingId: String,
    private val iotClientHelper: IoTClientHelper,
    private val mqttHelper: MqttManagerHelper,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private var dataObserver: DataObserver? = null
    private val awsMqttManager = mqttHelper.getMqttManager()
    private var keyStore: KeyStore? = null
    private val tag = "MqttViewModel"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val keyStoreFile = File("${appContext.filesDir}/keystore.bks")
            if (keyStoreFile.exists()) {
                initializeWithExistingKeyStore()
            } else {
                initializeWithNewKeyStore(appContext)
            }
            val isConnected = connectToMqttManager()
            _isConnected.value = isConnected
            isConnected
        } catch (e: Exception) {
            Log.e(tag, "Initialization failed: ${e.message}", e)
            initializeWithNewKeyStore(appContext)
            val isConnected = connectToMqttManager()
            _isConnected.value = isConnected
            isConnected
        }
    }

    private fun initializeWithExistingKeyStore() {
        keyStore = mqttHelper.getKeyStore(appContext)
        Log.d(tag, "KeyStore found and initialized.")
    }

    private fun initializeWithNewKeyStore(context: Context) {
        try {
            val awsKeyAndCert = iotClientHelper.getKeyAndCert()
            iotClientHelper.registerDevice(context, awsKeyAndCert)
            mqttHelper.createKeyStore(context, awsKeyAndCert)
            keyStore = mqttHelper.getKeyStore(context)
            Log.d(tag, "New KeyStore created and initialized.")
        } catch (e: Exception) {
            Log.e(tag, "Failed to create new KeyStore: ${e.message}", e)
        }
    }

    private suspend fun connectToMqttManager(): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            awsMqttManager.connect(keyStore) { status, throwable ->
                when {
                    throwable != null -> {
                        Log.e(tag, "MQTT connection error: ${throwable.message}", throwable)
                        if (continuation.isActive) continuation.resumeWithException(throwable)
                    }

                    status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected -> {
                        _isConnected.value = true
                        Log.d(tag, "MQTT connected.")
                        if (continuation.isActive) continuation.resume(true)
                    }

                    status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> {
                        _isConnected.value = false
                        Log.d(tag, "MQTT connection lost.")
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
        }
    }

    fun disconnectMqttManager() {
        awsMqttManager.disconnect()
        _isConnected.value = false
        Log.d(tag, "MQTT disconnected.")
    }

    fun cctvShadow(context: Context, groupId: Int) {
        val shadowTopics = listOf(
            "\$aws/things/${thingId}/shadow/update/delta",
            "\$aws/things/${thingId}/shadow/update/accepted",
            "\$aws/things/${thingId}/shadow/update/rejected",
            "\$aws/things/${thingId}/shadow/update/documents",
            "\$aws/things/${thingId}/shadow/get/accepted",
            "\$aws/things/${thingId}/shadow/get/rejected"
        )

        shadowTopics.forEach { topic ->
            subscribe(topic) { receivedTopic, message ->
                cctvShadowReceiveHandler(receivedTopic, message)
            }
        }

        val groupTopic = "/mhn/command/device/info/groups/$groupId"
        subscribe(groupTopic) { receivedTopic, message ->
            cctvReceiveHandler(receivedTopic, message, context)
        }
        createShadow()
        Log.d(tag, "Shadow subscriptions and publication complete.")
    }

    fun groupThingsSub(thingId: String, data: (ReportedThingInfo) -> Unit) {
        val topic = "/mhn/command/device/info/things/$thingId"
        subscribe(topic) { _, message ->
            viewerReceiveHandler(message) { stringMessage ->
                data(stringMessage)
            }
        }
    }


    private fun cctvReceiveHandler(receivedTopic: String, message: String, context: Context) {
        try {

            when {
                receivedTopic.contains("groups") -> {
                    val payload = DeviceUtils.getPublishPayload(context)
                    publish("/mhn/command/device/info/things/$thingId", payload)
                }

                receivedTopic.contains("things") -> {
                    if (message == "information") {
                        val payload = DeviceUtils.getPublishPayload(context)
                        publish("/mhn/command/device/info/things/$thingId", payload)
                    }
                }

                else -> {
                    Log.w(tag, "Unhandled topic: $receivedTopic")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to process topic message: ${e.message}", e)
        }
    }

    private fun viewerReceiveHandler(message: String, data: (ReportedThingInfo) -> Unit) {
        try {
            if (message != "information") {
                val json = Json { ignoreUnknownKeys = true }
                val reportedData: ReportedThingInfo = json.decodeFromString(message)
                data(reportedData)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to handle thing topic: ${e.message}", e)
        }
    }

    //group screen shadow handler
    private fun groupPageHandleShadowMessage(
        topic: String,
        message: String,
        data: (ReportedData?) -> Unit
    ) {
        try {
            // ignoreUnknownKeys 옵션 활성화
            val json = Json { ignoreUnknownKeys = true }

            // JSON 메시지 디코딩
            val device: DeviceInfoShadow = json.decodeFromString(message)
            // JSON 파싱 시 ignoreUnknownKeys = true 설정
            val reportedData: ReportedData? = device.state?.reported
            when {
                topic.contains("thingId") -> {
                    Log.d(tag, "Accepted 메시지 수신: $message")
                }

                else -> {
                    Log.w(tag, "Unhandled Shadow Topic: $topic")
                }
            }
            data(reportedData)
        } catch (e: Exception) {
            Log.e(tag, "Failed to process shadow message: ${e.message}", e)
        }
    }

    private fun cctvShadowReceiveHandler(topic: String, message: String) {
        try {
            when {
                topic.contains("delta") -> {
                    val jsonObject: ShadowDeltaMsg = try {
                        Json { ignoreUnknownKeys = true }.decodeFromString(message)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to decode shadow message: ${e.message}", e)
                        return
                    }
                    if (jsonObject.state?.delta?.kvsChannelDeleteRequested == true) {
                        iotClientHelper.deleteDevice()
                        Log.d(tag, "IoT Device 삭제 성공")
                    } else {
                        Log.d(tag, "Delta 처리 완료: $jsonObject")
                    }
                }

                else -> {
                    Log.w(tag, "Unhandled Shadow Topic: $topic")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to process shadow message: ${e.message}", e)
        }
    }

    fun cctvInfoSub(context: Context) {
        subscribe("/mhn/command/device/info/things/$thingId") { receivedTopic, message ->
            cctvReceiveHandler(receivedTopic, message, context)
        }
    }

    fun cctvInfoRequestPub(thingId: String) {
        publish("/mhn/command/device/info/things/$thingId", "information")
    }

    fun groupInfoRequestPub(payload: String, groupId: Long) {
        val topic = "/mhn/command/device/info/groups/$groupId"
        publish(topic, payload)
    }

    fun cctvAiResultPub(payload: String) {
        val topic = "/mhn/event/detect/things/$thingId"
        publish(topic, payload)
    }

    private fun createShadow() {
        publish("\$aws/things/${thingId}/shadow/update", DeviceUtils.getShadowPayload(appContext))
    }

    private fun updateShadow(payload: String) {
        val topic = "\$aws/things/${thingId}/shadow/update"
        try {
            awsMqttManager.publishString(payload, topic, AWSIotMqttQos.QOS0)
            Log.d(tag, "Published Shadow Update: $payload")
        } catch (e: Exception) {
            Log.e(tag, "Failed to publish shadow update: ${e.message}", e)
        }
    }

    fun startObservingData(context: Context) {
        if (dataObserver == null) {
            dataObserver = DataObserver(context).apply {
                startObserving()

                CoroutineScope(Dispatchers.IO).launch {
                    batteryLevel.collect { newBatteryLevel ->
                        val payload = DeviceUtils.getBatteryPayload(newBatteryLevel)
                        updateShadow(payload)
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    networkStatus.collect { newNetworkStatus ->
                        val payload = DeviceUtils.getNetworkPayload(newNetworkStatus)
                        updateShadow(payload)
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    kvsChannelActive.collect { isActive ->
                        val payload = DeviceUtils.getKvsChannelPayload(isActive)
                        updateShadow(payload)
                    }
                }
            }
        }
    }

    fun stopObservingData() {
        dataObserver = null // todo. mqtt 연결 해제될 때 같이 수정
    }

    fun eventTopic(
        trackingId: Int,
        objectName: String,
        confidence: Float,
        coordinates: List<Map<String, Int>>
    ) {
        val payload = coordinates.firstOrNull().let { coord ->
            """
        {
            "trackingId": $trackingId,
            "timestamp": ${System.currentTimeMillis() / 1000},
            "objectType": "$objectName",
            "confidence": $confidence,
            "coordinates": {
                "x1": ${coord!!["x1"]}, "y1": ${coord["y1"]},
                "x2": ${coord["x2"]}, "y2": ${coord["y2"]},
                "x3": ${coord["x3"]}, "y3": ${coord["y3"]},
                "x4": ${coord["x4"]}, "y4": ${coord["y4"]}
            }
        }
        """.trimIndent()
        }

        publish("/mhn/event/detect/things/$thingId", payload)
    }


    //shadow
    fun subscribeShadowWithPayload(thingId: String, data: (ReportedData?) -> Unit) {
        val topic = "\$aws/things/${thingId}/shadow/update/accepted"
        Log.d(tag, "Subscribe shadow accepted: \$aws/things/${thingId}/shadow/update/accepted")
        try {
            subscribe(topic, { receivedTopic, message ->
                Log.d(tag, "Message received on topic $receivedTopic: $message")
                groupPageHandleShadowMessage(receivedTopic, message) { reportedData ->
                    data(reportedData)
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Failed to publish shadow get: ${e.message}", e)
        }
    }

    private fun publish(topic: String, payload: String) {
        try {
            awsMqttManager.publishString(payload, topic, AWSIotMqttQos.QOS0)
            Log.d(tag, "Published to topic $topic: \n $payload")
        } catch (e: Exception) {
            Log.e(tag, "Failed to publish message: ${e.message}", e)
        }
    }

    private fun subscribe(topic: String, onMessageReceived: (String, String) -> Unit) {
        awsMqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0) { receivedTopic, message ->
            onMessageReceived(receivedTopic, message.toString(Charsets.UTF_8))
        }
    }
}

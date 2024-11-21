package com.example.mhnfe.mqtt

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import java.io.File
import kotlin.concurrent.thread

class MqttUtils :ViewModel() {
    private var awsMqttManager: AWSIotMqttManager? = null

    // IoT 클라이언트 초기화 함수
    fun initializeWithContext(context: Context) {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        thread {
            try {
                val iotClientHelper = IoTClientHelper(androidId)
                val keyStoreFile = File("${context.filesDir}/keystore.bks")
                val mqttHelper = MqttManagerHelper(androidId)

                if (keyStoreFile.exists()) {
                    try {
                        val keyStore = mqttHelper.getKeyStore(context)
                        awsMqttManager = mqttHelper.createMqttManager(context, keyStore)
                    } catch (e: Exception) {
                        Log.e("CameraViewModel", "KeyStore access error: ${e.message}", e)
                        initializeWithNewKeyStore(iotClientHelper, mqttHelper,context)
                    }
                } else {
                    initializeWithNewKeyStore(iotClientHelper, mqttHelper,context)
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error occurred: ${e.message}", e)
            }
        }
    }

    fun initializeWithNewKeyStore(
        iotClientHelper: IoTClientHelper,
        mqttHelper: MqttManagerHelper,
        context: Context
    ) {
        val awsKeyAndCert = iotClientHelper.getKeyAndCert()
        iotClientHelper.registerDevice(context, awsKeyAndCert)
        mqttHelper.createKeyStore(context, awsKeyAndCert)
        val keyStore = mqttHelper.getKeyStore(context)
        awsMqttManager = mqttHelper.createMqttManager(context, keyStore)
    }
}
package com.example.mhnfe.domain.mqtt

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult
import com.example.mhnfe.BuildConfig
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttManagerHelper @Inject constructor(
    private val thingId: String
) {
    private val tag = "MqttManagerHelper"
    private val keyStoreFilePath = "keystore.bks"

    @Volatile
    private var mqttManager: AWSIotMqttManager? = null

    companion object {
        private const val PREFS_NAME = "IoTPreferences"
        private const val CERTIFICATE_ID_KEY = "certificateId"
    }

    fun createKeyStore(context: Context, result: CreateKeysAndCertificateResult) {
        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(
            result.certificateId,
            result.certificatePem,
            result.keyPair.privateKey,
            context.filesDir.absolutePath,
            keyStoreFilePath,
            BuildConfig.AWS_KEYSTORE_PW
        )
        saveCertificateId(context, result.certificateId)
        Log.d(tag, "KeyStore created and saved: ${result.certificateId}")
    }

    fun getKeyStore(context: Context): KeyStore? {
        val certificateId = getStoredCertificateId(context)
        return if (certificateId != null) {
            AWSIotKeystoreHelper.getIotKeystore(
                certificateId,
                context.filesDir.absolutePath,
                keyStoreFilePath,
                BuildConfig.AWS_KEYSTORE_PW
            )
        } else {
            Log.e(tag, "No certificate ID found in preferences.")
            null
        }
    }

    fun getMqttManager(): AWSIotMqttManager {
        // Double-checked locking
        return mqttManager ?: synchronized(this) {
            mqttManager ?: createMqttManager().also {
                mqttManager = it
            }
        }
    }

    private fun createMqttManager(): AWSIotMqttManager {
        return AWSIotMqttManager(thingId, BuildConfig.MQTT_END_POINT).apply {
            isAutoReconnect = true
        }
    }

    private fun saveCertificateId(context: Context, certificateId: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(CERTIFICATE_ID_KEY, certificateId)
            apply()
        }
    }

    private fun getStoredCertificateId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(CERTIFICATE_ID_KEY, null)
    }
}

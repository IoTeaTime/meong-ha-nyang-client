package com.example.mhnfe.ui.screens.cctv

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mhnfe.mqtt.MqttUtils
import org.json.JSONObject

class CctvMqttViewModel : ViewModel() {
    private val tag = "CctvMqttViewModel"

    fun connectMqtt(context: Context, thingId: String, groupId: String) {
        try {
            MqttUtils.initialize(context)
            Log.d(tag, "MQTT 연결 성공: ThingId=$thingId, GroupId=$groupId")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 연결 실패: ${e.message}")
        }
    }

    fun disconnectMqtt() {
        try {
            MqttUtils.disconnectMqttManager()
            Log.d(tag, "MQTT 연결 해제")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 연결 해제 실패: ${e.message}")
        }
    }

    // 이벤트 탐지 시 MQTT 메시지 발행
    fun publishEvent(thingId: String) {
        try {
            val topic = "/mhn/event/detect/things/$thingId"
            val eventExample = """
            {
                "trackingId": 123,
                "timestamp": ${System.currentTimeMillis() / 1000},
                "objectType": "ani",
                "location": {
                    "x1": 30,
                    "y1": 0,
                    "x2": 800,
                    "y2": 700,
                    "x3": 900,
                    "y3": 700,
                    "x4": 1200,
                    "y4": 0
                }
            }
        """.trimIndent()
            MqttUtils.publish(topic, eventExample)
            Log.d(tag, "MQTT 메시지 발행 성공: Topic=$topic, Payload=$eventExample")
        } catch (e: Exception) {
            Log.e(tag, "MQTT 메시지 발행 실패: ${e.message}", e)
        }
    }


}

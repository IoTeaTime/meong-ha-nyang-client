package com.example.mhnfe.domin.mqtt

import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IoTShadowClientHelper @Inject constructor(
    private val thingId: String,
    private val mqttClientConnection: MqttClientConnection
) {
    private val client: IotShadowClient = IotShadowClient(mqttClientConnection)
}
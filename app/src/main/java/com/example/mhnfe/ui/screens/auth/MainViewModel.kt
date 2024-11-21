package com.example.mhnfe.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.mhnfe.mqtt.MqttUtils


class MainViewModel: ViewModel() {
    private var mqttUtils: MqttUtils = MqttUtils()
    fun initializeWithContext(context: Context){
        mqttUtils.initializeWithContext(context)
    }
}
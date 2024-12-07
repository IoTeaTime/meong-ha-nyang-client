package com.example.mhnfe.data.model

data class CCTV(
    val id: Long,
    val deviceName: String,
    val thingId: String,
    val channelName: String,
    //mqtt에서 받아올 것들
    var model: String = "Flip5",
    var os: String = "Android 10",
    var appVersion: String = "Android 10",
    var networkStatus: String = "양호",
    var batteryStatus: Int = 100
)

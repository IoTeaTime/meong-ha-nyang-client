package com.example.mhnfe
import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import org.webrtc.PeerConnectionFactory

/**
 webRTC라이브러리 초기화
 */
@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // WebRTC 초기화
        initializeWebRTC()
    }

    private fun initializeWebRTC() {
        try {
            val options = PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions()

            PeerConnectionFactory.initialize(options)
            Log.d("WebRTC", "PeerConnectionFactory 초기화 성공")
        } catch (e: Exception) {
            Log.e("WebRTC", "PeerConnectionFactory 초기화 실패", e)
        }
    }
}
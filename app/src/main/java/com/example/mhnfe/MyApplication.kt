package com.example.mhnfe
import android.app.Application
import android.util.Log
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import org.json.JSONException
import org.webrtc.PeerConnectionFactory

/**
 webRTC라이브러리 초기화
 */
class MyApplication : Application() {
    companion object {
        init {
            try {
                System.loadLibrary("webrtc_jni")

                // 추가 WebRTC 관련 네이티브 라이브러리들
                System.loadLibrary("webrtc")
                System.loadLibrary("webrtc_common")
                System.loadLibrary("webrtc_apm")

                Log.d("WebRTC", "WebRTC 라이브러리 로드 성공")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("WebRTC", "WebRTC 라이브러리 로드 실패", e)
            }
        }
    }

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
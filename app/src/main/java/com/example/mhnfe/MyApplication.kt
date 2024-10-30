package com.example.mhnfe

import android.app.Application
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeWebRTC()
    }
    //Initialize the library
    private fun initializeWebRTC() {
        try {
            System.loadLibrary("jingle_peerconnection_so")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("WebRTC", "Failed to load native library", e)
            e.printStackTrace()
        }
    }
}
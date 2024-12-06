package com.example.mhnfe

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mhnfe.ui.theme.MhnFETheme
import androidx.lifecycle.lifecycleScope
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.example.mhnfe.ui.navigation.AppNavigation
import com.example.mhnfe.utils.PermissionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.OpenCVLoader
import java.util.concurrent.CountDownLatch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)

        // OpenCV 초기화
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV 초기화 실패")
        } else {
            Log.d("OpenCV", "OpenCV 초기화 성공")
        }

        val auth = AWSMobileClient.getInstance()
        initializeMobileClient(auth, this@MainActivity)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AWSMobileClient.getInstance().signIn("peach3139@naver.com", "qqqq11", null)
            }
        }

        //Cognito 로그아웃
        // AWSMobileClient.getInstance().signOut()
        //권한 요청
        permissionManager.checkAndRequestPermissions()

        // FCM 토큰 확인
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            // SharedPreferences에 FCM 토큰 저장
            val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("fcm_token", token)
                apply()
            }
            Log.d(TAG, "FCM 토큰 저장됨: $token")  // 토큰 저장 확인 로그 추가
        })

        setContent {
            MhnFETheme {
                AppNavigation()

            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (::permissionManager.isInitialized) {
            permissionManager.checkAndRequestPermissions()
        }
    }
}

//Cognito 인증 초기화 (json 파일 사용)
private fun initializeMobileClient(client: AWSMobileClient, context: ComponentActivity) {
    val latch = CountDownLatch(1)
    client.initialize(context, object : Callback<UserStateDetails> {
        override fun onResult(result: UserStateDetails) {
            Log.d(
                "awskinesisvideo",
                "onResult: user state: " + result.userState
            )
            latch.countDown()
        }

        override fun onError(e: Exception) {
            Log.e(
                "awskinesisvideo",
                "onError: Initialization error of the mobile client",
                e
            )
            latch.countDown()
        }
    })
    try {
        latch.await()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

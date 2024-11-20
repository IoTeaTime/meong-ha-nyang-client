package com.example.mhnfe

import androidx.compose.ui.Modifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.theme.MhnFETheme
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import android.Manifest
import android.content.ContentValues.TAG
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.data.model.request.RequestFcmToken
import com.example.mhnfe.data.service.FcmService
import com.example.mhnfe.ui.navigation.AppNavigation
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.screens.master.KVSSignalingViewModel
import com.example.mhnfe.ui.screens.master.WebRTCUiState
import com.example.mhnfe.ui.screens.master.WebRtcConfig
import com.example.mhnfe.utils.PermissionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import org.webrtc.RendererCommon
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.CountDownLatch

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var fcmService: FcmService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
        fcmService = FcmService()

        val auth = AWSMobileClient.getInstance()
        initializeMobileClient(auth, this@MainActivity)
        //로그아웃
//        AWSMobileClient.getInstance().signOut()
        //권한 요청
        permissionManager.checkAndRequestPermissions()

        // FCM 토큰 확인
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            } else {
                val token = task.result
                // SharedPreferences에 FCM 토큰 저장
                val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("fcm_token", token)
                    apply()
                }
                Log.d(TAG, "FCM 토큰 저장됨: $token")  // 토큰 저장 확인 로그 추가
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            // TODO. 로그용, 로그인 구현 성공 후 리팩토링 필요
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            // TODO. 기기별로 발급되는 FCM 토큰을 발급 받은 후 이를 저장해 두었다가 로그인에 성공하면 saveFcmToken() 실행 필요 (하단 메서드)
            /*
            lifecycleScope.launch {
                try {
                    fcmService.saveFcmToken(RequestFcmToken(token))
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving FCM token", e)
                }
            }
            */
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

//class WebRtcViewModelFactory(
//    private val context: Context,
//    private val notificationManager: NotificationManager,
//    private val kvsSignalingViewModel: KVSSignalingViewModel
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WebRtcViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return WebRtcViewModel(kvsSignalingViewModel, context, notificationManager) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//private const val WEBRTC_VIEW_MODEL_KEY = "webrtc_view_model"


@Composable
fun SignalingChannelTest(
    navController: NavController,
    kvsViewModel: KVSSignalingViewModel = viewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        kvsViewModel.initialize(context)
    }


    var channelName by remember { mutableStateOf("demo-channel") }
    val scope = rememberCoroutineScope()
    val kvsState by kvsViewModel.uiState.collectAsState()
    val webRtcConfig by kvsViewModel.webRtcConfig.collectAsState()

    // 상태가 변경될 때마다 실행되는 효과
    LaunchedEffect(kvsState) {
        when (kvsState) {
            is WebRTCUiState.Success -> {
                val successState = kvsState as WebRTCUiState.Success

                when (successState.role) {
                    ChannelRole.MASTER -> {
                        navController.navigate(NavRoutes.Monitoring.Master.route)
                    }
                    ChannelRole.VIEWER -> {
                        navController.navigate(NavRoutes.Monitoring.Viewer.route)
                    }
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = channelName,
            onValueChange = { channelName = it },
            label = { Text("채널 이름") },
            modifier = Modifier.fillMaxWidth()
        )

        // Master 버튼
        Button(
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set("channelName", channelName)
                navController.currentBackStackEntry?.savedStateHandle?.set("role", ChannelRole.MASTER)
                navController.navigate(NavRoutes.Monitoring.Master.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("마스터로 입장")
        }

        // Viewer 버튼
        Button(
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set("channelName", channelName)
                navController.currentBackStackEntry?.savedStateHandle?.set("role", ChannelRole.VIEWER)
                navController.navigate(NavRoutes.Monitoring.Viewer.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("시청자로 입장")
        }

        // 현재 상태 표시
        when (kvsState) {
            WebRTCUiState.Loading -> {
                CircularProgressIndicator()
            }
            is WebRTCUiState.Error -> {
                Text(
                    text = (kvsState as WebRTCUiState.Error).message,
                    color = Color.Red
                )
            }
            else -> {}
        }
    }
}
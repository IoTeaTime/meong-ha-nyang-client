package com.example.mhnfe

import androidx.compose.ui.Modifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.theme.MhnFETheme
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.ui.navigation.AppNavigation
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.screens.master.KVSSignalingViewModel
import com.example.mhnfe.ui.screens.master.WebRTCUiState
import com.example.mhnfe.utils.PermissionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)

        val auth = AWSMobileClient.getInstance()
        initializeMobileClient(auth, this@MainActivity)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AWSMobileClient.getInstance().signIn("peach3139@naver.com", "qqqq11", null)
            }
        }

        //로그아웃
//        AWSMobileClient.getInstance().signOut()
        //권한 요청
        permissionManager.checkAndRequestPermissions()

//        // FCM 토큰 확인
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
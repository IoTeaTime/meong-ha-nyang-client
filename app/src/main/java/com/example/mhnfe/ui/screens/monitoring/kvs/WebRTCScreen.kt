package com.example.mhnfe.ui.screens.monitoring.kvs

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.R
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.screens.auth.main.MainViewModel
import com.example.mhnfe.ui.screens.auth.main.RunningDogLoadingAnimation
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.EglBase
import org.webrtc.Logging

@Composable
@SuppressLint("HardwareIds")
fun WebRtcScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    channelName: String,
    role: ChannelRole,
    viewModel: KVSSignalingViewModel,
    mqttViewModel: MqttViewModel = hiltViewModel(),
    aiViewModel: AiViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val localView by viewModel.localView.collectAsState()
    val remoteView by viewModel.remoteView.collectAsState()
    val eglBase = remember { EglBase.create() }
    val connectionEvent by viewModel.connectionEvent.collectAsState()
    val isViewsInitialized by viewModel.isViewsInitialized.collectAsState()
    val mqttState by mqttViewModel.isConnected.collectAsState()
    val window = (context as? Activity)?.window
    val isCameraSwitching by viewModel.isCameraSwitching.collectAsState()
    val isRecording = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var groupId = 0
        mainViewModel.getCctvAccessToken { cctvAccessToken ->
            if(cctvAccessToken != "") {
                mainViewModel.fetchCctvId(
                    onSuccess = { cctvInfo ->
                        Log.d(
                            "MainScreen",
                            "Loaded CCTV Info: ${cctvInfo.body.cctvNickname}, CCTV ID: ${cctvInfo.body.cctvId}"
                        )
                        groupId = cctvInfo.body.groupId
                    },
                    onFailure = { fetchError ->
                        Log.e("MainScreen", "자동 로그인 실패 및 CCTV ID 확인 실패", fetchError)
                    }
                )
            }
        }

        if (role == ChannelRole.MASTER && !mqttState) {
            val result = mqttViewModel.initialize()
            if (result) {
                if (groupId != 0) {
                    mqttViewModel.cctvShadow(context, groupId)
                }
                mqttViewModel.cctvInfoSub(context)
            }
        }
        if (role == ChannelRole.MASTER && mqttState) {
            aiViewModel.detectEvent(
                onResult = { trackingId, coordinatesJson, objectName, confidence ->
                    mqttViewModel.eventTopic(trackingId, coordinatesJson, objectName, confidence)
                }
            )
            mqttViewModel.startObservingData(context)
        }
    }

    // 초기화 한 번만 실행을 위한 key 사용
    LaunchedEffect(channelName) {
        if (uiState !is WebRTCUiState.Success) {
            try {
                viewModel.initialize(context)
                viewModel.updateSignalingChannelInfo(
                    channelName = channelName,
                    role = role,
                    context = context,
                    eglBase = eglBase.eglBaseContext
                ).join()

                viewModel.initializeSurfaceViews(context, eglBase.eglBaseContext, role)
            } catch (e: Exception) {
                Log.e("WebRTCScreen", "초기화 실패", e)
                Toast.makeText(context, "연결 초기화 실패", Toast.LENGTH_SHORT).show()
                navController.navigateUp()
            }
        }
    }


    LaunchedEffect(connectionEvent) {
        Log.d("WebRtcScreen", "connectionEvent 상태: $connectionEvent")
        when (connectionEvent) {
            ConnectionEvent.ConnectionFailed -> {
                Toast.makeText(context, "연결 실패", Toast.LENGTH_LONG).show()
                navController.navigateUp()
                viewModel.onConnectionEventHandled()
            }

            ConnectionEvent.ConnectionSuccess -> {
                Log.d("WebRtcScreen", "연결 성공: MQTT 초기화 시작")
                viewModel.onConnectionEventHandled()
            }

            null -> {}
        }
    }

    // 리소스 정리 함수
    val cleanup = {
        try {
            Logging.enableLogToDebugOutput(Logging.Severity.LS_NONE)
            viewModel.resetState()
            localView?.let {
                it.clearImage()
                it.setMirror(false)
                it.release()
            }
            remoteView?.let {
                it.clearImage()
                it.release()
            }
            viewModel.updateState(WebRTCUiState.Initial)
            Log.d("WebRTCScreen", "cleanup 완료")
        } catch (e: Exception) {
            Log.e("WebRTCScreen", "리소스 정리 실패", e)
        }
    }

    // 뒤로 가기 처리
    BackHandler {
        Log.d("WebRTCScreen", "BackHandler 실행")
        viewModel.viewModelScope.launch {
            try {
                viewModel.releasePeerConnection()
                cleanup()

                withContext(Dispatchers.Main) {
                    navController.navigate("monitoring/group") {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            } catch (e: Exception) {
                Log.e("WebRTCScreen", "연결 해제 실패", e)
                withContext(Dispatchers.Main) {
                    navController.navigate("monitoring/group") {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            }
        }
    }
//    DisposableEffect(Unit) {
//        onDispose {
//            viewModel.viewModelScope.launch {
//                    viewModel.releasePeerConnection()
//                    cleanup()
//            }
//        }
//    }

    Log.d("WebRtcScreen", "channelName: $channelName, role: $role")

    var isDimmed by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // 밝기 조절 함수
    fun adjustBrightness(dim: Boolean) {
        window?.let {
            val params = it.attributes
            params.screenBrightness = if (dim) 0.0f else 1.0f
            it.attributes = params
        }
        isDimmed = dim
    }

    // 10초 후 자동으로 어둡게 하는 타이머
    LaunchedEffect(lastInteractionTime) {
        if (role == ChannelRole.MASTER) {
        delay(10000) // 10초 대기
        adjustBrightness(true)
        }

    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = mainBlack)
            // 마스터일 때만 터치 이벤트 감지
            .then(
                if (role == ChannelRole.MASTER) {
                    modifier.pointerInput(Unit) {
                        detectTapGestures {
                            if (isDimmed) {
                                adjustBrightness(false)
                            }
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    }
                } else {
                    modifier
                }
            )
    ) {
        Box(
            modifier = modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when (uiState) {
                is WebRTCUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column (
                            modifier.fillMaxSize().background(color = Color.White),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
                        ) {
                            RunningDogLoadingAnimation()
                            Text(
                                text = "화면 연결중...",
                                style = Typography.labelLarge,
                                color = mainBlack
                            )
                        }
                    }
                }

                is WebRTCUiState.Success -> {
                    if (role == ChannelRole.MASTER) {
                        //ai
                        LaunchedEffect(Unit) {
                            try {
                                viewModel.frameData
                                    .onEach { bitmap ->
                                        if (!isCameraSwitching) {
                                            bitmap?.let {
                                                withContext(Dispatchers.Default) {
                                                    aiViewModel.processFrame(it)
                                                }
                                            } ?: Log.d("WebRtcScreen", "Received null bitmap")
                                        } else {
                                            Log.d("WebRtcScreen", "Skipping frame processing due to camera switching")
                                        }
                                    }
                                    .catch { e ->
                                        Log.e("WebRtcScreen", "Error collecting frames", e)
                                    }
                                    .launchIn(this)
                            } catch (e: Exception) {
                                Log.e("WebRtcScreen", "Frame collection failed", e)
                            }
                        }
                        //UI
                        if (isViewsInitialized) {
                            localView?.let { renderer ->
                                //카메라 항상 켜짐
                                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                AndroidView(
                                    factory = {
                                        renderer.apply {
                                            (parent as? android.view.ViewGroup)?.removeView(this)
                                        }
                                    },
                                    modifier = modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        // Viewer
                        if (isViewsInitialized) {
                            remoteView?.let { renderer ->
                                AndroidView(
                                    factory = {
                                        renderer.apply {
                                            (parent as? ViewGroup)?.removeView(this)
                                        }
                                    },
                                    modifier = modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Row(
                        modifier = modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = modifier
                                .size(42.dp),
                            onClick = {
                                viewModel.viewModelScope.launch {
                                    try {
                                        viewModel.releasePeerConnection()
                                        cleanup()

                                        withContext(Dispatchers.Main) {
                                            navController.navigate("monitoring/group") {
                                                popUpTo(navController.graph.findStartDestination().id)
                                                launchSingleTop = true
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("WebRTCScreen", "연결 해제 실패", e)
                                        withContext(Dispatchers.Main) {
                                            navController.navigate("monitoring/group") {
                                                popUpTo(navController.graph.findStartDestination().id)
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                painter = painterResource(id = R.drawable.exit),
                                contentDescription = "연결 종료",
                                tint = Color.Unspecified
                            )
                        }
                    }
                    Row(
                        modifier = modifier
                            .padding(20.dp)
                            .align(Alignment.BottomCenter)
                            .wrapContentHeight()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(
                            modifier = Modifier.size(50.dp)
                        )
                        if (role == ChannelRole.VIEWER) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.captureScreen(context)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "",
                                    color = Color.Black,
                                    style = Typography.bodySmall
                                )
                            }
                        }else {
                            Text(
                                modifier = modifier.background(Color.White, shape = CircleShape).padding(10.dp),
                                text = "10초 후 절전 모드가 실행됩니다",
                                color = mainBlack,
                                style = Typography.labelSmall,
                            )
                        }

                        //카메라 전환
                        IconButton(
                            modifier = modifier
                                .size(50.dp),
                            onClick = {
                                viewModel.switchCamera(context)
                            }
                        ) {
                            Icon(
                                modifier = modifier.size(41.dp),
                                painter = painterResource(id = R.drawable.switch_refresh),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                }

                is WebRTCUiState.Error -> {
                    Row(
                        modifier = modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (uiState as WebRTCUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> { /* 다른 상태 처리 */
                }
            }
        }
    }
}

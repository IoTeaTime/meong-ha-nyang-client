package com.example.mhnfe.ui.screens.monitoring.kvs

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.mhnfe.ui.theme.mainBlack
import kotlinx.coroutines.Dispatchers
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
    viewModel: KVSSignalingViewModel,
    navController: NavController,
    channelName: String,
    role: ChannelRole,
    mqttViewModel: MqttViewModel = hiltViewModel(),
    aiViewModel: AiViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val localView by viewModel.localView.collectAsState()
    val remoteView by viewModel.remoteView.collectAsState()
    val eglBase = remember { EglBase.create() }
    val connectionEvent by viewModel.connectionEvent.collectAsState()
    val isViewsInitialized by viewModel.isViewsInitialized.collectAsState()
    var aiResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {

            // Todo. 역할을 가져오는 로직 추가
            val roles = ChannelRole.MASTER
            // Todo. 그룹 ID를 가져오는 로직 추가
            val groupId = 404

            withContext(Dispatchers.IO) {
                // 1. MQTT 연결 시도 (MASTER일 때)
                val isConnected = withContext(Dispatchers.IO) {
                    mqttViewModel.initialize(context)
                }
                if (isConnected)
                    if (roles == ChannelRole.MASTER)
                        mqttViewModel.createShadowWithSubscribe(context, groupId)
            }
        } catch (e: Exception) {
            Log.e("WebRTCScreen", "MQTT 연결 테스트 실패 또는 구독 실패", e)
            Toast.makeText(context, "MQTT 연결 실패 또는 구독 실패", Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (role == ChannelRole.MASTER) {
                try {
                    mqttViewModel.disconnectMqttManager()
                    Log.d("WebRTCScreen", "MQTT Manager Disconnected for MASTER role")
                } catch (e: Exception) {
                    Log.e("WebRTCScreen", "Failed to disconnect MQTT Manager", e)
                }
            }
        }
    }

    // 초기화는 한 번만 실행되도록 key를 사용
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

    // 뒤로가기 처리
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


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = mainBlack)
    ) {
        Row(
            modifier = modifier
                .background(color = Color.Transparent)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        try {
                            viewModel.releasePeerConnection()
                            cleanup()

                            withContext(Dispatchers.Main) {
                                navController.navigateUp()
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
                Text("카메라 끄기")
            }

            // MQTT 이벤트 발행 및 AI 분석 button
            Button(
                onClick = {
                    // AI 분석 및 MQTT 이벤트 발행
//                    viewModel.viewModelScope.launch {
//                        aiViewModel.simulateAIProcessing(
//                            onResult = { result ->
//                                Log.d("WebRTCScreen", "AI Result: $result")
//                            },
//                            onPayloadReady = { payload ->
//                                try {
//                                    mqttViewModel.publishAIResult(payload) // MQTT 이벤트 발행
//                                    Toast.makeText(context, "MQTT 이벤트 발행 완료", Toast.LENGTH_SHORT)
//                                        .show()
//                                } catch (e: Exception) {
//                                    Log.e("WebRTCScreen", "MQTT 이벤트 발행 실패", e)
//                                }
//                            }
//                        )
//                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("AI 이벤트")
            }

            IconButton(
                modifier = modifier
                    .size(50.dp),
                onClick = {
                    if (role == ChannelRole.MASTER) {
                        viewModel.switchCamera(context)
                    }
                }
            ) {
                Icon(
                    modifier = modifier.size(41.dp),
                    painter = painterResource(id = R.drawable.switch_camera),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
        if (role == ChannelRole.MASTER) {
            LaunchedEffect(Unit) {
                try {
                    viewModel.frameData
                        .onEach { bitmap ->
                            bitmap?.let {
                                withContext(Dispatchers.Default) {
                                    aiViewModel.processFrame(it)
                                }
                            } ?: Log.d("WebRtcScreen", "Received null bitmap")
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
            Box(
                modifier = modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                if (isViewsInitialized) {
                    localView?.let { renderer ->
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
            }
        } else {
            // Viewer
            Box(
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isViewsInitialized) {
                    remoteView?.let { renderer ->
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
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            when (uiState) {
                is WebRTCUiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                }

                is WebRTCUiState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Role: ${(uiState as WebRTCUiState.Success).role}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is WebRTCUiState.Error -> {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
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

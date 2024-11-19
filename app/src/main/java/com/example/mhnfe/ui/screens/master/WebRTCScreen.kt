package com.example.mhnfe.ui.screens.master

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import org.webrtc.EglBase

@Composable
fun WebRtcScreen(
    viewModel: KVSSignalingViewModel,
    navController: NavController,
    channelName: String,
    role: ChannelRole
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val localView by viewModel.localView.collectAsState()
    val remoteView by viewModel.remoteView.collectAsState()


    // EglBase를 컴포저블 레벨에서 생성
    val eglBase = remember { EglBase.create() }

    val connectionEvent by viewModel.connectionEvent.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(context)

        viewModel.updateSignalingChannelInfo(
            channelName = channelName,
            role = role,
            context = context,
            eglBase = eglBase.eglBaseContext
        ).join()

        viewModel.initializeSurfaceViews(context, eglBase.eglBaseContext, role)
        //viewModel.initWsConnection(true)

    }
    // 연결 이벤트 처리
    LaunchedEffect(connectionEvent) {

        when (connectionEvent) {
            ConnectionEvent.ConnectionFailed -> {
                Toast.makeText(context, "Connection error to signaling", Toast.LENGTH_LONG).show()
                navController.navigateUp()  // 화면 종료
                viewModel.onConnectionEventHandled()
            }
            null -> {}
            ConnectionEvent.ConnectionSuccess -> TODO()
        }
    }
    val isViewsInitialized by viewModel.isViewsInitialized.collectAsState()

    // SurfaceViewRenderer 초기화 및 정리
    DisposableEffect(lifecycleOwner) {
        Log.d("DisposableEffect", "Entered DisposableEffect block")
        // role이 있을 때만 WebSocket 연결 시작
        if (uiState is WebRTCUiState.Success) {
            Log.d("DisposableEffect", "uiState is Success. ")

            //viewModel.initializeSurfaceViews(context, (uiState as WebRTCUiState.Success).rootEglBase)
            val isMaster = (uiState as WebRTCUiState.Success).role == ChannelRole.MASTER

        }

        onDispose {
            localView?.release()
            remoteView?.release()
            eglBase.release()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isViewsInitialized) {
                val isMaster = role == ChannelRole.MASTER

                if (isMaster) {
                    // 마스터: 로컬 뷰만 전체 화면으로
                    localView?.let { renderer ->
                        AndroidView(
                            factory = { renderer },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // 뷰어: 리모트 뷰 전체화면 + 로컬 뷰 PIP
                    remoteView?.let { renderer ->
                        AndroidView(
                            factory = { renderer },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    localView?.let { renderer ->
                        AndroidView(
                            factory = { renderer },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(120.dp)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // 하단에 상태 및 컨트롤 UI
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

                else -> { /* 다른 상태 처리 */ }
            }
        }
    }
}

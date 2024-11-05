package com.example.mhnfe.ui.screens.master

import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer

@Composable
fun WebRtcScreen(
    context: Context,
    notificationManager: NotificationManager,
    channelName: String,
    isMaster: Boolean,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {

        Box(modifier = modifier.fillMaxSize()) {

            // Remote View (Background)
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                    }
                },
                update = { view ->
                }
            )

            // Local View (Floating)
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        setEnableHardwareScaler(true)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .draggable(
                        state = rememberDraggableState { delta -> /* Handle drag state */ },
                        orientation = Orientation.Vertical
                    ),
                update = { view ->
                }
            )

    }
}

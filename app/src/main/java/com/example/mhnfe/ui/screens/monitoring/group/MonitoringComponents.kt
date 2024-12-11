package com.example.mhnfe.ui.screens.monitoring.group

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mhnfe.R
import com.example.mhnfe.data.model.CCTV
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainGray3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun CCTVItemCard(
    cctv: CCTV,
    groupId: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
    mqttViewModel: MqttViewModel = hiltViewModel()
) {
    val thingId = cctv.thingId
    var networkStatus by remember { mutableStateOf(1) }
    var batteryStatus by remember { mutableStateOf(0) }
    val scope = CoroutineScope(Dispatchers.Main)
    val interval: Long = 10_000


    LaunchedEffect(Unit) {
        //shadow sub
        mqttViewModel.groupShadowSub(thingId) { reportedData ->
            reportedData.let {
                if (it != null && it.isBackCamera == null) {
                    if(it.networkStatus == null){
                        batteryStatus = it.batteryLevel!!
                    }else{
                        networkStatus = it.networkStatus

                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        //topic pub sub
        var job = scope.launch {
            var messageReceived: Boolean
            mqttViewModel.groupThingsSub(thingId) { reportedData ->
                // 메시지 수신 시 처리
                reportedData.let {
                    networkStatus = it.networkStatus
                    batteryStatus = it.batteryLevel
                }
                // 메시지가 도착했음을 플래그로 표시하고 타임아웃 Job 취소
                messageReceived = true
            }
            while (isActive) {
                Log.d("MonitoringComponents", "Send Topic to get device info")
                // Group Info Request Publish
                mqttViewModel.groupInfoRequestPub("", groupId)
                // 메시지가 도착했는지 확인하는 플래그
                messageReceived = false
                // 메시지 타임아웃을 처리하기 위한 Job
                delay(interval)
                val timeoutJob = launch {
                    if (!messageReceived) {
                        // 메시지가 없으면 네트워크와 배터리를 0으로 설정
                        networkStatus = 0
                        batteryStatus = 0
                    }
                }
                delay(interval) // 다음 요청 전 대기
                timeoutJob.cancel()
            }
        }
        onDispose { job.cancel() }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = mainGray3
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cctv.deviceName,
                style = Typography.bodyMedium
            )
            Row(
                modifier = modifier
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = modifier.size(24.dp)
                ) {
                    Icon(
                        modifier = modifier
                            .size(20.dp, 16.dp)
                            .align(Alignment.Center),
                        painter = painterResource(id = if(networkStatus >= 2) R.drawable.good_signal else R.drawable.bad_signal),
                        contentDescription = "signal",
                        tint = Color.Unspecified
                    )
                }
                Box(
                    modifier = modifier.size(24.dp)
                ) {
                    Icon(
                        modifier = modifier
                            .size(20.dp, 16.dp)
                            .align(Alignment.Center),
                        painter = painterResource(
                            id = when {
                                //배터리 양 별로 다른 아이콘
                                batteryStatus >= 80 -> R.drawable.full_battery
                                batteryStatus >= 40 -> R.drawable.medium_battery
                                else -> R.drawable.low_battery
                            }),
                        contentDescription = "편집"
                    )
                }
                IconButton(
                    modifier = modifier.size(24.dp),
                    onClick = onEdit
                ) {
                    Icon(
                        modifier = modifier.size(6.dp, 20.dp),
                        painter = painterResource(id = R.drawable.cctv_setting),
                        contentDescription = "편집"
                    )
                }
            }

        }
    }
}
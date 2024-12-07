package com.example.mhnfe.ui.screens.monitoring

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mhnfe.R
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.screens.monitoring.device.DeviceViewModel
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray2
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    cctvId: Long,
    navController: NavController,
    mqttViewModel: MqttViewModel = hiltViewModel(),
    deviceViewModel: DeviceViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val cctv by deviceViewModel.cctv.collectAsState()

    var mqttStatus by remember { mutableStateOf(false) }
    var deviceModel by remember { mutableStateOf("") }
    var osVersion by remember { mutableStateOf("") }
    var appVersion by remember { mutableStateOf("") }
    var batteryStatus by remember { mutableStateOf(0) }
    var networkStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        deviceViewModel.getCctvInfo(cctvId) { cctvInfo ->
            mqttViewModel.groupThingsSub(cctvInfo.thingId) { thingInfo ->
                deviceModel = thingInfo.deviceModel
                osVersion = thingInfo.osVersion
                appVersion = thingInfo.appVersion
                batteryStatus = thingInfo.batteryLevel
                networkStatus = mqttViewModel.convertNetworkStatusToString(thingInfo.networkStatus)
                Log.e("Thing Subscribe", "battery : " + batteryStatus)
                Log.e("Thing Subscribe", "network : " + batteryStatus)

                mqttStatus = true
            }
            mqttViewModel.cctvInfoRequestPub(cctvInfo.thingId)
            mqttViewModel.groupShadowSub(cctvInfo.thingId) { thingInfo ->
                thingInfo?.let {
                    batteryStatus = thingInfo.batteryLevel!!
                    networkStatus = mqttViewModel.convertNetworkStatusToString(thingInfo.networkStatus!!)
                    Log.e("Thing Shadow Subscribe", "battery : " + batteryStatus)
                    Log.e("Thing Shadow Subscribe", "network : " + batteryStatus)

                    mqttStatus = true
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val job = scope.launch {
            while (isActive) {
                cctv?.let {
                    mqttViewModel.cctvInfoRequestPub(it.thingId)
                    mqttStatus = true
                }
                delay(120_000)
            }
        }
        onDispose { job.cancel() }
    }

    Scaffold(
        topBar = {
            SubTopBar(text = "기기 정보", onBack = { navController.popBackStack()})
        },
    ) { innerPadding ->
        if (cctv != null) {
            if (!mqttStatus) {
               // MQTT가 연결되지 않은 경우
                Box(
                    modifier = modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator() // 로딩 표시
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "기기 연결을 시도 중입니다...",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        modifier = modifier.fillMaxWidth().wrapContentHeight()
                            .padding(vertical = 45.dp, horizontal = 34.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(
                            40.dp,
                            alignment = Alignment.CenterVertically
                        )
                    ) {
                        DeviceInfoCard(
                            title = "기기 정보",
                            label1 = "기기명",
                            label2 = "기종",
                            value1 = cctv!!.deviceName,
                            value2 = deviceModel
                        )
                        DeviceInfoCard(
                            title = "버전 관리",
                            label1 = "OS",
                            label2 = "앱 버전",
                            value1 = osVersion,
                            value2 = appVersion
                        )
                        DeviceInfoCard(
                            title = "연결 상태",
                            label1 = "배터리",
                            label2 = "네트워크 상태",
                            value1 = if (batteryStatus.toString() == "0") "연결 대기 중..." else batteryStatus.toString(),
                            value2 = if (networkStatus == "") "연결 대기 중..." else networkStatus
                        )
                        Box(
                            modifier = modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo2),
                                contentDescription = "로고",
                                modifier = modifier.size(147.dp, 168.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        } else {
            Column (
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    style = Typography.bodyMedium,
                    text = "CCTV 정보를 찾을 수 없습니다.",
                    color = mainBlack
                )
            }
        }
    }
}

//기기 정보 UI
@Composable
private fun DeviceInfoCard(
    modifier: Modifier = Modifier,
    title : String,
    label1: String, value1: String,
    label2: String, value2: String
){
    Column(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(title)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = mainGray2, shape = RoundedCornerShape(8.dp))
            ,
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label1,
                    modifier = modifier.width(120.dp)
                )
                Text(
                    text = value1,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.Start)
                )
            }
            HorizontalDivider(color = Color.White, thickness = 1.dp)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label2,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = value2,
                    modifier = modifier
                        .wrapContentWidth(Alignment.Start)
                )
            }
        }
    }

}

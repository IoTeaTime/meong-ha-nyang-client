package com.example.mhnfe.ui.screens.monitoring.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.data.model.CCTV
import com.example.mhnfe.data.remote.request.CctvInfo
import com.example.mhnfe.di.UserType
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.components.SmallButton
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    userType: UserType = UserType.MASTER,
    navController: NavController,
    mqttViewModel: MqttViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var connectionState: Boolean = false
    val groupInfo by groupViewModel.groupInfo.collectAsState()

    LaunchedEffect(Unit) {
        connectionState = mqttViewModel.testTopic()
    }

    LaunchedEffect(connectionState) {

        val thingList = listOf("53f6de0c846034b8", "fd72414d2c21c071")
        mqttViewModel.viewerInitialSubscribe(context, thingList)
        groupViewModel.fetchGroupInfo {
            groupInfo->
            val groupName = groupInfo.groupName
            val groupId = groupInfo.groupId
            val payload = """
            {
                "groupInfo": "$groupName",
                "timestamp": ${System.currentTimeMillis() / 1000}
            }
            """.trimIndent()
            mqttViewModel.getDeviceInfo(payload, groupId)
        }



    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(text = groupInfo?.groupName ?: "그룹")
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(vertical = 28.dp, horizontal = 34.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                40.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            //마스터 화면 일 때 버튼 추가
            if (userType == UserType.MASTER) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmallButton(
                        onClick = {
                            navController.navigate(
                                NavRoutes.Monitoring.QRGenerate.createRoute(UserType.CCTV)
                            )
                        },
                        text = "CCTV 추가"
                    )
                    SmallButton(
                        onClick = {
                            navController.navigate(
                                NavRoutes.Monitoring.QRGenerate.createRoute(UserType.VIEWER)
                            )
                        },
                        text = "참여자 추가"
                    )
                }
            }
            val cctvList = groupInfo?.cctv ?: emptyList()
            if (cctvList.isEmpty()) {
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        style = Typography.bodyMedium,
                        text = "등록된 CCTV가 없습니다.",
                        color = mainBlack
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = cctvList,
                        key = { it.cctvId }
                    ) { cctvItem ->
                        CCTVItemCard(
                            cctv = cctvItem.toCCTV(),
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "role",
                                    ChannelRole.VIEWER
                                )
                                navController.navigate(
                                    NavRoutes.Monitoring.Viewer.createRoute(
                                        channelName = cctvItem.kvsChannelName
                                    )
                                )
                            },
                            onEdit = {
                                navController.navigate(
                                    NavRoutes.Monitoring.DeviceInformation.createRoute(
                                        cctvItem.cctvId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

fun CctvInfo.toCCTV(): CCTV {
    return CCTV(
        id = cctvId,
        deviceName = cctvNickname,
        thingId = thingId,
        channelName = kvsChannelName
    )
}



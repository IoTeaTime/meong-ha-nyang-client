package com.example.mhnfe.ui.screens.mypage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.data.remote.response.CctvInfo
import com.example.mhnfe.data.remote.response.GroupMemberInfo
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.theme.Typography

@Composable
fun DeviceManagementScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    deviceManagementViewModel: DeviceManagementViewModel = hiltViewModel()
) {
    val errorMessage by deviceManagementViewModel.errorMessage.collectAsState()
    val groupMemberInfoList by deviceManagementViewModel.groupMemberInfoList.collectAsState()
    val cctvList by deviceManagementViewModel.cctvList.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if(!errorMessage.isNullOrBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                deviceManagementViewModel.clearErrorMessage()
            }
        }
    }

    LaunchedEffect(Unit) {
        // 그룹 CCTV 정보 리스트 조회
        deviceManagementViewModel.getCctvList()

        // 그룹 회원 정보 리스트 조회
        deviceManagementViewModel.getGroupMemberList()
    }

    var cctvDevices by remember {
        mutableStateOf(cctvList)
    }

    var viewerDevices by remember {
        mutableStateOf(groupMemberInfoList)
    }

    LaunchedEffect(cctvList) {
        cctvDevices = cctvList
    }
    LaunchedEffect(groupMemberInfoList) {
        viewerDevices = groupMemberInfoList
    }

    Scaffold(
        topBar = {
            SubTopBar(
                text = "기기 관리 페이지",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // CCTV Section
            Text(
                text = "CCTV 기기 관리",
                style = Typography.titleMedium,
                modifier = modifier.padding(vertical = 16.dp)
            )

            // CCTV Devices
            cctvDevices?.forEach { device ->
                CctvDeviceItem(
                    device = device,
                    onDelete = {
                        deviceManagementViewModel.deleteDevice(device.cctvId)
                        cctvDevices = cctvDevices!!.filter { it.cctvId != device.cctvId } },
                    onUpdate = { updatedDevice ->
                        deviceManagementViewModel.changeCctvName(updatedDevice.cctvId, updatedDevice.cctvNickname)
                        cctvDevices = cctvDevices!!.map {
                            if (it.cctvId == updatedDevice.cctvId) {
                                updatedDevice
                            } else it
                        }
                    }
                )
                Spacer(modifier = modifier.height(8.dp))
            }

            // Viewer Section
            Text(
                text = "Viewer 기기 관리",
                style = Typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Viewer Devices
            viewerDevices?.forEach { device ->
                ViewerDeviceItem(
                    device = device,
                    onDelete = {
                        deviceManagementViewModel.deleteViewer(device.groupMemberId)
                        viewerDevices =
                            viewerDevices!!.filter { it.memberId != device.memberId }
                    },
                    onUpdate = { updatedDevice ->
                        viewerDevices = viewerDevices!!.map {
                            if (it.memberId == updatedDevice.memberId) updatedDevice else it
                        }
                    }
                )
                Spacer(modifier = modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ViewerDeviceItem(
    modifier: Modifier = Modifier,
    device: GroupMemberInfo,
    onDelete: () -> Unit,
    onUpdate: (GroupMemberInfo) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.nickname,
                    style = Typography.bodyMedium
                )
            }

            Text (
                text = "기기삭제",
                style = Typography.bodySmall.copy(color = Color.Gray),
                modifier = modifier.clickable { onDelete() }
            )
        }
    }

    if (showEditDialog) {
        EditDeviceDialog(
            initialName = device.nickname,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                onUpdate(device.copy(nickname = newName))
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun CctvDeviceItem(
    modifier: Modifier = Modifier,
    device: CctvInfo,
    onDelete: () -> Unit,
    onUpdate: (CctvInfo) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.cctvNickname,
                    style = Typography.bodyMedium
                )

                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "수정",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { showEditDialog = true },
                    tint = Color.Gray
                )
            }

            Text (
                text = "기기삭제",
                style = Typography.bodySmall.copy(color = Color.Gray),
                modifier = modifier.clickable { onDelete() }
            )
        }
    }

    if (showEditDialog) {
        EditDeviceDialog(
            initialName = device.cctvNickname,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                onUpdate(device.copy(cctvNickname = newName))
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditDeviceDialog(
    modifier: Modifier = Modifier,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var deviceName by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "기기 이름 수정",
                    style = Typography.titleMedium
                )

                TextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    modifier = modifier.fillMaxWidth(),
                    label = { Text("기기 이름") },
                    singleLine = true
                )

                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    TextButton(onClick = { onConfirm(deviceName) }) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceManagementScreenPreview() {
    val navController = rememberNavController()
    DeviceManagementScreen(navController = navController)
}
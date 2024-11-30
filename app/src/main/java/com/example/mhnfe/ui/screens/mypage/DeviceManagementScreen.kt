package com.example.mhnfe.ui.screens.mypage

import EditPopup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.data.remote.request.CctvInfo
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
) {
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
                text = "참여자 삭제",
                style = Typography.bodySmall.copy(color = Color.Gray),
                modifier = modifier.clickable { onDelete() }
            )
        }
    }
}

@Composable
private fun CctvDeviceItem(
    modifier: Modifier = Modifier,
    device: CctvInfo,
    onDelete: () -> Unit,
    onUpdate: (CctvInfo) -> Unit
) {
    val (showEditDialog, setShowEditDialog) = remember { mutableStateOf(false) }

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
                        .clickable { setShowEditDialog(true) },
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
        EditPopup(
            onDismissRequest = { setShowEditDialog(false) },
            onConfirmation = { newName ->
                if (newName != null) {
                    onUpdate(device.copy(cctvNickname = newName))
                }
                setShowEditDialog(false)
            },
            isDialogVisible = false,
            initialText = device.cctvNickname
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceManagementScreenPreview() {
    val navController = rememberNavController()
    DeviceManagementScreen(navController = navController)
}
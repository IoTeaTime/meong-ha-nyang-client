package com.example.mhnfe.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainGray2

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType
)

enum class DeviceType {
    CCTV,
    VIEWER
}

@Composable
fun DeviceManagementScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var cctvDevices by remember {
        mutableStateOf(listOf(
            Device("1", "주방", DeviceType.CCTV),
            Device("2", "거실", DeviceType.CCTV)
        ))
    }

    var viewerDevices by remember {
        mutableStateOf(listOf(
            Device("3", "V1", DeviceType.VIEWER),
            Device("4", "V2", DeviceType.VIEWER),
            Device("5", "V3", DeviceType.VIEWER)
        ))
    }

    Scaffold(
        topBar = {
            SubTopBar(text = "기기 관리 페이지", onBack = { navController.popBackStack() })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DeviceSection(
                title = "CCTV 기기 관리",
                devices = cctvDevices,
                onDeleteDevice = { device ->
                    cctvDevices = cctvDevices.filter { it.id != device.id }
                },
                onUpdateDevice = { updatedDevice ->
                    cctvDevices = cctvDevices.map {
                        if (it.id == updatedDevice.id) updatedDevice else it
                    }
                }
            )
            DeviceSection(
                title = "Viewer 기기 관리",
                devices = viewerDevices,
                onDeleteDevice = { device ->
                    viewerDevices = viewerDevices.filter { it.id != device.id }
                },
                onUpdateDevice = { updatedDevice ->
                    viewerDevices = viewerDevices.map {
                        if (it.id == updatedDevice.id) updatedDevice else it
                    }
                }
            )
        }
    }
}

@Composable
private fun DeviceSection(
    modifier: Modifier = Modifier,
    title: String,
    devices: List<Device>,
    onDeleteDevice: (Device) -> Unit,
    onUpdateDevice: (Device) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = Typography.bodyMedium
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = mainGray2, shape = RoundedCornerShape(8.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            devices.forEachIndexed { index, device ->
                if (index > 0) {
                    HorizontalDivider(color = Color.White, thickness = 1.dp)
                }
                DeviceItem(
                    device = device,
                    onDelete = { onDeleteDevice(device) },
                    onUpdate = { newName ->
                        onUpdateDevice(device.copy(name = newName))
                    }
                )
            }
        }
    }
}

@Composable
private fun DeviceItem(
    modifier: Modifier = Modifier,
    device: Device,
    onDelete: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = device.name,
                style = Typography.bodyMedium
            )
            // 연필 아이콘
            Icon(
                painter = painterResource(id = R.drawable.edit),  // 연필 아이콘 리소스 필요
                contentDescription = "수정",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { showEditDialog = true }
            )
        }

        TextButton(
            onClick = onDelete,
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Gray
            )
        ) {
            Text(
                text = "기기 삭제",
                style = Typography.bodySmall
            )
        }
    }

    if (showEditDialog) {
        EditDeviceDialog(
            initialName = device.name,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                onUpdate(newName)
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "기기 이름 수정",
                    style = Typography.titleMedium
                )

                TextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("기기 이름") },
                    singleLine = true
                )

                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
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
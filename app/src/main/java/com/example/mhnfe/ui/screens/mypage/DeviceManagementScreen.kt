package com.example.mhnfe.ui.screens.mypage

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.theme.Typography
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
    navController: NavController,
    deviceManagementViewModel: DeviceManagementViewModel = hiltViewModel()
) {
    val errorMessage by deviceManagementViewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if(!errorMessage.isNullOrBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                deviceManagementViewModel.clearErrorMessage()
            }
        }
    }

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
            cctvDevices.forEach { device ->
                DeviceItem(
                    device = device,
                    onDelete = {
                        deviceManagementViewModel.deleteDevice(device.id.toLong())
                        cctvDevices = cctvDevices.filter { it.id != device.id } },
                    onUpdate = { updatedDevice ->
                        cctvDevices = cctvDevices.map {
                            if (it.id == updatedDevice.id) {
                                deviceManagementViewModel.changeCctvName(updatedDevice.id.toLong(),updatedDevice.name)
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
            viewerDevices.forEach { device ->
                DeviceItem(
                    device = device,
                    onDelete = { viewerDevices = viewerDevices.filter { it.id != device.id } },
                    onUpdate = { updatedDevice ->
                        viewerDevices = viewerDevices.map {
                            if (it.id == updatedDevice.id) updatedDevice else it
                        }
                    }
                )
                Spacer(modifier = modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DeviceItem(
    modifier: Modifier = Modifier,
    device: Device,
    onDelete: () -> Unit,
    onUpdate: (Device) -> Unit
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
                    text = device.name,
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

            Text(
                text = "기기삭제",
                style = Typography.bodySmall.copy(color = Color.Gray),
                modifier = modifier.clickable { onDelete() }
            )
        }
    }

    if (showEditDialog) {
        EditDeviceDialog(
            initialName = device.name,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                onUpdate(device.copy(name = newName))
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
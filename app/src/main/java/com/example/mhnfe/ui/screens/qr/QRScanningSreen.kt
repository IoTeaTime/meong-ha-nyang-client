package com.example.mhnfe.ui.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mhnfe.ui.screens.cctv.CameraState

@Composable
fun QRScanningScreen(
    modifier: Modifier = Modifier,
    viewModel: QRScanningViewModel = viewModel()
) {
    val context = LocalContext.current

    val cameraState = remember { mutableStateOf<CameraState>(CameraState.PermissionNotGranted) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraState.value = CameraState.Success
        }
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) -> {
                cameraState.value = CameraState.Success
            }
            else -> {
                cameraLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQRScanningScreen() {
    QRScanningScreen()
}
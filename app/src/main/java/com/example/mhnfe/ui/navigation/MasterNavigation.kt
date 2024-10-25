package com.example.mhnfe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.ui.screens.master.MasterMainScreen
import com.example.mhnfe.ui.screens.master.QRGenerateScreen
import com.example.mhnfe.ui.screens.master.QRViewModel

//NavGraph에서 사용할 route 상수 정의
object NavRoutes {
    const val MASTER_MAIN = "master_main"
    const val QR_GENERATE = "qr_generate"
}

@Composable
fun MasterNavigation(){
    val navController = rememberNavController()
    val viewModel: QRViewModel = viewModel()

    NavHost(navController= navController, startDestination = NavRoutes.MASTER_MAIN ) {
        composable(NavRoutes.MASTER_MAIN) {
            MasterMainScreen(
                qrViewModel = viewModel,
                onQRButtonClick = {
                    navController.navigate(NavRoutes.QR_GENERATE) {
                        popUpTo(NavRoutes.MASTER_MAIN)  // 이전 스택을 모두 제거
                        launchSingleTop = true // 화면 중복 생성 방지
                    }
                }
            )
        }
        composable(NavRoutes.QR_GENERATE) {
            QRGenerateScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}
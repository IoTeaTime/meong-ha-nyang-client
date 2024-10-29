package com.example.mhnfe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.ui.screens.cctv.CCTVScreen
import com.example.mhnfe.ui.screens.cctv.CameraViewModel
import com.example.mhnfe.ui.screens.master.MasterMainScreen
import com.example.mhnfe.ui.screens.master.QRGenerateScreen
import com.example.mhnfe.ui.screens.master.QRViewModel

//NavGraph에서 사용할 route 상수 정의
object NavRoutes {
    const val MASTER_MAIN = "master_main"
    const val QR_GENERATE = "qr_generate"
    const val CCTV_STREAM = "cctv_stream"

    fun cctvStream(streamId: String) = "cctv_stream/$streamId"
}

@Composable
fun MasterNavigation(){
    val navController = rememberNavController()
    val viewModel: QRViewModel = viewModel()

    NavHost(navController= navController, startDestination = NavRoutes.MASTER_MAIN ) {
        //master_main
        composable(NavRoutes.MASTER_MAIN) {
            MasterMainScreen(
                qrViewModel = viewModel,
                onQRButtonClick = {
                    navController.navigate(NavRoutes.QR_GENERATE) {
                        popUpTo(NavRoutes.MASTER_MAIN)  // 이전 스택을 모두 제거
                        launchSingleTop = true // 화면 중복 생성 방지
                    }
                },
                onCCTVButtonClick = {
                    navController.navigate(NavRoutes.CCTV_STREAM)  // CCTV 화면으로 네비게이션
                },
                onViewerButtonClick = {}
            )
        }
        //qr_generate
        composable(NavRoutes.QR_GENERATE) {
            QRGenerateScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(NavRoutes.CCTV_STREAM) {
            CCTVScreen(
                onNavigateBack = {
                    navController.popBackStack()  // 이전 화면으로 돌아가기
                }
            )
        }
        //cctv_stream
//        composable(
//            route = "cctv_stream/{streamId}",
//            arguments = listOf(navArgument("streamId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val streamId = backStackEntry.arguments?.getString("streamId")
//            CCTVScreen(streamName = streamId ?: StreamNames.CCTV_ENTRANCE)
//        }
    }
}
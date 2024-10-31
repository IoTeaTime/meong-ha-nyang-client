package com.example.mhnfe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.amazonaws.mobile.client.AWSMobileClient
import com.example.mhnfe.ui.screens.auth.StartUpScreen
import com.example.mhnfe.ui.screens.master.MasterMainScreen
import com.example.mhnfe.ui.screens.master.QRViewModel





sealed class NavRoutes(val route: String) {
    object Auth : NavRoutes("auth") {
        object Login : NavRoutes("login")
        object SignUp : NavRoutes("signup")
    }

    object Main : NavRoutes("main") {
        object Home : NavRoutes("home")
        object CCTV : NavRoutes("cctv")
        object Viewer : NavRoutes("viewer")
    }
}
@Composable
fun AppNavigation() {
    val auth = remember { AWSMobileClient.getInstance() }
    val navController = rememberNavController()
    val viewModel: QRViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Auth.route
    ) {
        // Auth Graph
        navigation(
            startDestination = NavRoutes.Auth.Login.route,
            route = NavRoutes.Auth.route
        ) {
            composable(NavRoutes.Auth.Login.route) {
                StartUpScreen(
                    auth = auth,
                    navController = navController
                )
            }

            composable(NavRoutes.Auth.SignUp.route) {
                // SignUp Screen
            }
        }

        // Main Graph
        navigation(
            startDestination = NavRoutes.Main.Home.route,
            route = NavRoutes.Main.route
        ) {
            composable(NavRoutes.Main.Home.route) {
                MasterMainScreen(
                qrViewModel = viewModel,
                onQRButtonClick = {
                    navController.navigate(NavRoutes.Main.CCTV.route) {
                        popUpTo(NavRoutes.Main.route)  // 이전 스택을 모두 제거
                        launchSingleTop = true // 화면 중복 생성 방지
                    }
                },
                onCCTVButtonClick = {
                    navController.navigate(NavRoutes.Main.CCTV.route)  // CCTV 화면으로 네비게이션
                },
                onViewerButtonClick = { navController.navigate(NavRoutes.Main.CCTV.route)}
            )
            }

            composable(NavRoutes.Main.CCTV.route) {
                CCTVScreen(
                    onBackPressed = {
                        navController.navigateUp()
                    }
                )
            }

            composable(NavRoutes.Main.Viewer.route) {
                ViewerScreen(
                    onBackPressed = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}


@Composable
fun CCTVScreen(
    onBackPressed: () -> Unit
) {
    // CCTVScreen 구현
}

@Composable
fun ViewerScreen(
    onBackPressed: () -> Unit
) {
    // ViewerScreen 구현
}

package com.example.mhnfe.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.amazonaws.mobile.client.AWSMobileClient
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.bottombar.BottomNavigationBar
import com.example.mhnfe.ui.screens.auth.StartUpScreen
import com.example.mhnfe.ui.screens.monitoring.DeviceInfoScreen
import com.example.mhnfe.ui.screens.monitoring.GroupScreen
import com.example.mhnfe.ui.screens.mypage.ProfileScreen
import com.example.mhnfe.ui.screens.qr.QRGenerateScreen
import com.example.mhnfe.ui.screens.qr.QRViewModel
import com.example.mhnfe.ui.screens.report.ReportDetailScreen

sealed class NavRoutes(val route: String) {
    object Auth : NavRoutes("auth") {
        object Login : NavRoutes("login")
        object SignUp : NavRoutes("signup")
        object Select : NavRoutes("select")
    }

    object Main : NavRoutes("main/{userType}") {
        fun createRoute(userType: UserType) = "main/$userType"
    }

    object Monitoring : NavRoutes("monitoring") {
        object Group : NavRoutes("monitoring/group")
        object DeviceInformation : NavRoutes("device_information/{cctvId}") {
            fun createRoute(cctvId: String) = "device_information/$cctvId"
        }
        object QRScanner : NavRoutes("qr_scanner")
        object QRGenerate : NavRoutes("qr_generate/{userType}") {
            fun createRoute(userType: UserType) = "qr_generate/${userType.name.lowercase()}"
        }
    }
    object Report : NavRoutes("report") {
        object ReportDetail : NavRoutes("report/report_detail")
        //추후에 화면이 추가 될 수 있기 때문에 이렇게 따로 빼서 구현 추후 화면 추가가 없을 시 삭제
    }
    object MyPage : NavRoutes("myPage") {
        object Profile : NavRoutes("myPage/profile")
        object ChangePassword : NavRoutes("myPage/change_password")
        object DeviceManagement : NavRoutes("myPage/device_management")
    }
}

@Composable
fun AppNavigation() {
    val auth = remember { AWSMobileClient.getInstance() }
    val navController = rememberNavController()

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
            composable(NavRoutes.Auth.Select.route) {
//                SelectScreen(onBackClick = {})
            }
        }

        // Main Content with BottomBar
        composable(
            route = NavRoutes.Main.route,
            arguments = listOf(
                navArgument("userType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userType = UserType.fromString(backStackEntry.arguments?.getString("userType"))
            MainContent(
                mainNavController = navController,
                auth = auth,
                userType = userType
            )
        }

        // 추가: 로그인에서 메인으로 네비게이션할 때 사용할 익스텐션 함수
        fun NavController.navigateToMain(userType: UserType) {
            navigate(NavRoutes.Main.createRoute(userType)) {
                popUpTo(NavRoutes.Auth.route) {
                    inclusive = true  // Auth 그래프를 백스택에서 완전히 제거
                }
            }
        }
    }
}


@Composable
fun MainContent(
    mainNavController: NavController,
    auth: AWSMobileClient,
    userType: UserType
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(
        NavRoutes.Monitoring.Group.route,
        NavRoutes.Report.ReportDetail.route,
        NavRoutes.MyPage.Profile.route
    )

    val showBottomBar = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = bottomNavController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,  // 여기서는 bottomNavController 사용
            startDestination = NavRoutes.Monitoring.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Monitoring Graph
            navigation(
                startDestination = NavRoutes.Monitoring.Group.route,
                route = NavRoutes.Monitoring.route
            ) {
                composable(NavRoutes.Monitoring.Group.route) {
                    GroupScreen(
                        userType = userType,
                        navController = bottomNavController  // bottomNavController 전달
                    )
                }
                composable(
                    route = NavRoutes.Monitoring.DeviceInformation.route,
                    arguments = listOf(
                        navArgument("cctvId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val cctvId = backStackEntry.arguments?.getString("cctvId") ?: return@composable
                    DeviceInfoScreen(
                        cctvId = cctvId,
                        navController = bottomNavController
                    )
                }
                composable(NavRoutes.Monitoring.QRScanner.route) {
                    // QRScannerScreen
                }
                composable(
                    route = NavRoutes.Monitoring.QRGenerate.route,
                    arguments = listOf(
                        navArgument("userType") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val qrUserType = backStackEntry.arguments?.getString("userType")
                    QRGenerateScreen(
                        navController = bottomNavController,  // bottomNavController 사용
                        userType = UserType.fromString(qrUserType)
                    )
                }
            }

            // Report Graph
            navigation(
                startDestination = NavRoutes.Report.ReportDetail.route,
                route = NavRoutes.Report.route
            ) {
                composable(NavRoutes.Report.ReportDetail.route) {
                    ReportDetailScreen(navController = bottomNavController)
                }
                //추후에 화면이 추가 될 수 있기 때문에 이렇게 따로 빼서 구현 추후 화면 추가가 없을 시 삭제
            }

            // MyPage Graph
            navigation(
                startDestination = NavRoutes.MyPage.Profile.route,
                route = NavRoutes.MyPage.route
            ) {
                composable(NavRoutes.MyPage.Profile.route) {
                    ProfileScreen(
                        navController = bottomNavController
                    )
                }
                composable(NavRoutes.MyPage.ChangePassword.route) {
                    // ChangePasswordScreen
                }
                composable(NavRoutes.MyPage.DeviceManagement.route) {
                    // DeviceManagementScreen
                }
            }
        }
    }
}



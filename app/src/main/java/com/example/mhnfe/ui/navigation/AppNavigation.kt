package com.example.mhnfe.ui.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.screens.auth.login.LoginScreen
import com.example.mhnfe.ui.screens.auth.main.MainScreen
import com.example.mhnfe.ui.screens.auth.select.SelectScreen
import com.example.mhnfe.ui.screens.auth.signup.SignUpScreen
import com.example.mhnfe.ui.screens.monitoring.kvs.KVSSignalingViewModel
import com.example.mhnfe.ui.screens.monitoring.kvs.WebRtcScreen
import com.example.mhnfe.ui.screens.monitoring.DeviceInfoScreen
import com.example.mhnfe.ui.screens.monitoring.group.GroupScreen
import com.example.mhnfe.ui.screens.mypage.PasswordEditScreen
import com.example.mhnfe.ui.screens.mypage.ProfileScreen
import com.example.mhnfe.ui.screens.qr.qrgenerate.QRGenerateScreen
import com.example.mhnfe.ui.screens.qr.qrscannig.QRScanningScreen
import com.example.mhnfe.ui.screens.mypage.DeviceManagementScreen
import com.example.mhnfe.ui.screens.report.ReportDetailScreen


sealed class NavRoutes(val route: String) {
    object Auth : NavRoutes("auth") {
        object Main : NavRoutes("main")
        object Login : NavRoutes("login")
        object SignUp : NavRoutes("signup")
        object Select : NavRoutes("select")
        object Master {
            const val route = "master/{channelName}"
            fun createRoute(channelName: String) = "master/$channelName"
        }
        object QRScanner {
            const val route = "qr_scanner/{userType}"
            fun createRoute(userType: UserType) = "qr_scanner/${userType.name}"
        }
    }

    object Main : NavRoutes("main/{userType}") {
        fun createRoute(userType: UserType) = "main/$userType"
    }

    object Monitoring : NavRoutes("monitoring") {
        object Group : NavRoutes("monitoring/group")
        object Master : NavRoutes("monitoring/master")
//        object Viewer : NavRoutes("monitoring/viewer")
        object Viewer {
            const val route = "monitoring/viewer/{channelName}"

            fun createRoute(channelName: String) = "monitoring/viewer/$channelName"
        }

        object DeviceInformation : NavRoutes("device_information/{cctvId}") {
            fun createRoute(cctvId: Long) = "device_information/$cctvId"
        }
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

// 추가: 로그인에서 메인으로 네비게이션할 때 사용할 익스텐션 함수
fun NavController.navigateToMain(userType: UserType) {
    navigate(NavRoutes.Main.createRoute(userType)) {
        popUpTo(NavRoutes.MyPage.route) {
            inclusive = true  // Auth 그래프를 백스택에서 완전히 제거
        }
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
            startDestination = NavRoutes.Auth.Main.route,
            route = NavRoutes.Auth.route
        ) {
            composable(NavRoutes.Auth.Main.route) {
                MainScreen(
                    navController = navController,
                )
            }
            composable(NavRoutes.Auth.Login.route) {
                LoginScreen(
                    navController = navController
                )
            }
            composable(NavRoutes.Auth.SignUp.route) {
                SignUpScreen(
                    navController = navController,
                    onLoginClick = {
                        navController.navigate(NavRoutes.Auth.Login.route) {
                            popUpTo(NavRoutes.Auth.Main.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.Auth.Select.route) {
                SelectScreen(
                    navController = navController)
            }
            composable(
                route = NavRoutes.Auth.QRScanner.route,
                arguments = listOf(
                    navArgument("userType") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userType = UserType.valueOf(
                    backStackEntry.arguments?.getString("userType") ?: UserType.VIEWER.name
                )
                QRScanningScreen(
                    navController = navController,
                    userType = userType
                )
            }
            //cctv화면
            composable(
                route = NavRoutes.Auth.Master.route,
                arguments = listOf(
                    navArgument("channelName") { type = NavType.StringType }
                )
            ) {
                backStackEntry ->
                val kvsSignalingViewModel: KVSSignalingViewModel = viewModel()

                // channelName을 arguments에서 읽기
                val channelName = backStackEntry.arguments?.getString("channelName") ?: "demo-channel"
                val role = ChannelRole.MASTER

                WebRtcScreen(
                    navController = navController,
                    viewModel = kvsSignalingViewModel,
                    channelName = channelName,
                    role = role
                )
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
                composable(NavRoutes.Monitoring.Master.route) {
                    val parentEntry = remember(bottomNavController) {
                        bottomNavController.getBackStackEntry(NavRoutes.Report.ReportDetail.route)
                    }
                    val kvsSignalingViewModel: KVSSignalingViewModel = viewModel(
                        viewModelStoreOwner = parentEntry
                    )

                    val channelName = parentEntry.savedStateHandle.get<String>("channelName") ?: "demo-channel"
                    val role = ChannelRole.MASTER  // Master route이므로 MASTER로 고정

                    WebRtcScreen(
                        navController = bottomNavController,
                        viewModel = kvsSignalingViewModel,
                        channelName = channelName,
                        role = role
                    )
                }
                composable(
                    route = NavRoutes.Monitoring.Viewer.route,
                    arguments = listOf(
                        navArgument("channelName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val channelName = backStackEntry.arguments?.getString("channelName") ?: "demo-channel"
                    val kvsSignalingViewModel: KVSSignalingViewModel = viewModel()

                    WebRtcScreen(
                        navController = bottomNavController,
                        viewModel = kvsSignalingViewModel,
                        channelName = channelName,
                        role = ChannelRole.VIEWER
                    )
                }
                composable(
                    route = NavRoutes.Monitoring.DeviceInformation.route,
                    arguments = listOf(
                        navArgument("cctvId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val cctvId = backStackEntry.arguments?.getLong("cctvId") ?: return@composable
                    DeviceInfoScreen(
                        cctvId = cctvId,
                        navController = bottomNavController
                    )
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
//                    entry ->
//                    val kvsViewModel: KVSSignalingViewModel = viewModel(viewModelStoreOwner = entry)
//                    SignalingChannelTest(
//                        navController = bottomNavController,
//                        kvsViewModel = kvsViewModel,
//                    )
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
                        bottomNavController= bottomNavController,
                        mainNavController = mainNavController
                    )
                }
                composable(NavRoutes.MyPage.ChangePassword.route) {
                    PasswordEditScreen(
                        bottomNavController = bottomNavController
                    )
                }

                composable(NavRoutes.MyPage.DeviceManagement.route) {
                    DeviceManagementScreen(
                        navController = bottomNavController
                    )
                }
            }
        }
    }
}
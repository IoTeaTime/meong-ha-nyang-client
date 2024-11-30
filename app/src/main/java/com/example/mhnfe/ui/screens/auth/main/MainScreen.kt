package com.example.mhnfe.ui.screens.auth.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.di.UserType
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.navigation.NavRoutes


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mqttViewModel: MqttViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()  // MainViewModel 주입
) {
    val loginState = remember { mutableStateOf<LoginState>(LoginState.Loading) }


    // 자동 로그인 시도와 페이지 변경을 분리
    LaunchedEffect(Unit) {
        mainViewModel.autoLogin(
            onSuccess = { role ->
                loginState.value = LoginState.Success(role)
            },
            onFailure = { error ->
                loginState.value = LoginState.Error(error)
            }
        )
    }

    LaunchedEffect(loginState.value) {
        when (val state = loginState.value) {
            is LoginState.Success -> {
                mqttViewModel.initialize()

                    // 로그인 성공 후 화면 전환
                    when (state.role) {
                        "ROLE_MASTER" -> {
                            navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        }

                        "ROLE_VIEWER" -> {
                            navController.navigate(NavRoutes.Main.createRoute(UserType.VIEWER)) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        }

                        else -> {
                            navController.navigate(NavRoutes.Auth.Select.route) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        }
                    }
                }


            is LoginState.Error -> {
                Log.e("MainScreen", "Auto Login Failed: ${state.error.message}")
            }

            else -> {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 20.dp, horizontal = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "명하냥 로고",
            modifier = modifier.size(315.dp, 358.dp)
        )

        // 버튼 영역
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로그인 버튼
            MiddleButton(
                text = " 로그인",
                onClick = {
                    navController.navigate(NavRoutes.Auth.Login.route)
//                    navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
//                        popUpTo(NavRoutes.Auth.route) { inclusive = true }
//                    }
                },
            )
            // Cam 회원 버튼
            MiddleButton(
                text = "회원가입",
                onClick = {
                    navController.navigate(NavRoutes.Auth.SignUp.route)
                },
            )
            MiddleButton(
                text = "Cam 참여",
                onClick = {
                    navController.navigate(NavRoutes.Auth.QRScanner.route)
                },
            )

            // 소셜 로그인 버튼들
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao),
                    contentDescription = "카카오 로그인",
                    modifier = modifier.size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.google1),
                    contentDescription = "구글 로그인",
                    modifier = modifier.size(50.dp)

                )
                Image(
                    painter = painterResource(id = R.drawable.naver),
                    contentDescription = "네이버 로그인",
                    modifier = modifier.size(50.dp)
                )
            }
        }
    }
}

@Preview(
    name = "Start Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun StartScreenPreview() {
    MainScreen(
        navController = rememberNavController(),
    )
}

sealed class LoginState {
    data object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val error: Exception) : LoginState()
}
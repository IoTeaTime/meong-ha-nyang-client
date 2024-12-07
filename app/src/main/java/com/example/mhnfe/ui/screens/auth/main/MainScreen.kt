package com.example.mhnfe.ui.screens.auth.main

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.R
import com.example.mhnfe.di.UserType
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.navigation.NavRoutes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.hoverYellow
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainYellow

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mqttViewModel: MqttViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()  // MainViewModel 주입
) {
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    LaunchedEffect(Unit) {
        val isAutoLoginEnabled = sharedPreferences.getBoolean("AUTO_LOGIN", false)
        if(isAutoLoginEnabled) {
            mqttViewModel.initialize()
            mainViewModel.autoLogin(
                onSuccess = { role, groupId ->
                    if (groupId != 0L) {
                        if (role == "ROLE_MASTER") {
                            navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(NavRoutes.Main.createRoute(UserType.VIEWER)) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        }
                    } else {
                        isLoading = false
                        navController.navigate(NavRoutes.Auth.Select.route) {
                            popUpTo(NavRoutes.Auth.route) { inclusive = true }
                        }
                        mqttViewModel.disconnectMqttManager()
                    }
                })
        } else {
            mainViewModel.getCctvAccessToken { cctvAccessToken ->
                if(cctvAccessToken != "") {
                    mainViewModel.fetchCctvId(
                        onSuccess = { cctvInfo ->
                            Log.d(
                                "MainScreen",
                                "Loaded CCTV Info: ${cctvInfo.body.cctvNickname}, CCTV ID: ${cctvInfo.body.cctvId}"
                            )
                            val channelName = cctvInfo.body.kvsChannelName

                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("channelName", channelName)
                                set("role", ChannelRole.MASTER)
                            }

                            navController.navigate(NavRoutes.Auth.Master.createRoute(channelName = channelName))
                        },
                        onFailure = { fetchError ->
                            isLoading = false
                            Log.e("MainScreen", "자동 로그인 실패 및 CCTV ID 확인 실패", fetchError)
                        }
                    )
                } else {
                    isLoading = false
                }
            }
        }
    }
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
            ) {
                RunningDogLoadingAnimation()
                Text(
                    text = "로딩중...",
                    style = Typography.labelLarge,
                    color = mainBlack
                )
            }
        }
    } else {
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
            modifier = modifier
                .size(315.dp, 358.dp)
                .padding(top = 60.dp)
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
                    navController.navigate(NavRoutes.Auth.QRScanner.createRoute(UserType.CCTV))
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
}


@Composable
fun RunningDogLoadingAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val legAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val earWiggle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier
            .size(150.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height

        // translate 제거하고 고정 위치로 변경
        // 몸통
        drawCircle(
            color = mainGray,
            radius = width * 0.2f,
            center = center.copy(x = width * 0.5f)  // x 위치를 중앙으로 조정
        )


        // 머리
        drawCircle(
            color = mainGray,
            radius = width * 0.2f,
            center = center.copy(x = width * 0.7f, y = height * 0.4f)  // x 위치 조정
        )

        // 왼쪽 귀
        val leftEarPath = Path().apply {
            moveTo(width * 0.60f, height * 0.3f)  // x 위치 조정
            lineTo(width * 0.6f + earWiggle, height * 0.15f)
            lineTo(width * 0.7f, height * 0.25f)
            close()
        }
        drawPath(
            path = leftEarPath,
            color = mainGray
        )

        // 오른쪽 귀
        val rightEarPath = Path().apply {
            moveTo(width * 0.70f, height * 0.3f)  // x 위치 조정
            lineTo(width * 0.7f + earWiggle, height * 0.15f)
            lineTo(width * 0.8f, height * 0.25f)
            close()
        }
        drawPath(
            path = rightEarPath,
            color = mainGray
        )

        // 눈
        drawCircle(
            color = Color.Black,
            radius = width * 0.02f,
            center = center.copy(x = width * 0.65f, y = height * 0.30f)  // x 위치 조정
        )

        // 코
        drawCircle(
            color = Color.Black,
            radius = width * 0.04f,
            center = center.copy(x = width * 0.9f, y = height * 0.4f)  // x 위치 조정
        )

        // 앞다리
        val frontLegPath = Path().apply {
            moveTo(width * 0.45f, height * 0.6f)  // x 위치 조정
            lineTo(width * 0.45f + legAngle, height * 0.8f)
        }
        drawPath(
            path = frontLegPath,
            color = mainGray,
            style = Stroke(
                width = width * 0.1f,
                cap = StrokeCap.Round
            )
        )

        // 뒷다리
        val backLegPath = Path().apply {
            moveTo(width * 0.55f, height * 0.6f)  // x 위치 조정
            lineTo(width * 0.55f - legAngle, height * 0.8f)
        }
        drawPath(
            path = backLegPath,
            color = mainGray,
            style = Stroke(
                width = width * 0.1f,
                cap = StrokeCap.Round
            )
        )

        // 꼬리
        val tailPath = Path().apply {
            moveTo(width * 0.35f, height * 0.4f)  // x 위치 조정
            quadraticBezierTo(
                width * 0.3f,
                height * 0.3f,
                width * 0.35f,
                height * 0.2f
            )
        }
        drawPath(
            path = tailPath,
            color = mainGray,
            style = Stroke(
                width = width * 0.08f,
                cap = StrokeCap.Round
            )
        )
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
    RunningDogLoadingAnimation()
//    PawLoadingAnimation()
//    MainScreen(
//        navController = rememberNavController(),
//    )
}
package com.example.mhnfe.ui.screens.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.data.repository.UserRepository
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.screens.cctv.CameraViewModel


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val savedId = sharedPreferences.getString("saved_id", null)
    val savedPassword = sharedPreferences.getString("saved_password", null)

    // 자동 로그인 로직
    LaunchedEffect(Unit) {
        if (!savedId.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            // 저장된 로그인 정보로 자동 로그인 시도
            val authRepository = AuthRepository()
            try {
                val response = authRepository.login(savedId, savedPassword)
                if (response.result.code == 200) {
                    // 자동 로그인 성공 -> 다음 화면으로 이동
                    navController.navigate(NavRoutes.Auth.Select.route) {
                        popUpTo(NavRoutes.Auth.route) { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainScreen", "자동 로그인 실패", e)
            }
        }
    }
//    viewModel.initializeWithContext(context)
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

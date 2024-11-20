package com.example.mhnfe.ui.screens.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.data.repository.AuthRepository
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainYellow

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(AuthRepository())),
    onLoginClick: () -> Unit
) {
    val loginResponse by loginViewModel.loginResponse.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()

    var loginError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    var isAutoLogin by remember { mutableStateOf(false) }

    // 로그인 입력값 상태 관리
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Cognito 로그인
//    val cognitoUsername = ""
//    val cognitoPassword = ""

    // Create an instance of the Repository for calling the login API
    val authRepository = AuthRepository()

    // SharedPreferences를 사용해 자동 로그인 상태와 사용자 정보를 저장
    val context = LocalContext.current
    val sharedPreferences = LocalContext.current.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    Scaffold(
        modifier = modifier,
        topBar = {
            SubTopBar(
                text = "로그인",
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단부 (로고)
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically)
            ) {
                // 로고 이미지
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "로고",
                    modifier = Modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )

                // 입력 필드들과 자동로그인을 포함하는 Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
                    horizontalAlignment = Alignment.Start
                ) {
                    MainTextBox(
                        focusManager = focusManager,
                        inputText = id,
                        onInputTextChange = { id = it },
                        isError = loginError,
                        onIsErrorChange = {loginError = it},
                        warningText = errorMessage ?: "",
                        hintText = "아이디"
                    )

                    MainTextBox(
                        focusManager = focusManager,
                        inputText = password,
                        onInputTextChange = { password = it },
                        isError = passwordError,
                        onIsErrorChange = {passwordError = it},
                        warningText = errorMessage ?: "",
                        hintText = "비밀번호",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    // 자동 로그인 체크박스
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .offset((-12).dp)
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    ) {
                        Checkbox(
                            checked = isAutoLogin,
                            onCheckedChange = { isAutoLogin = it },
                            colors = CheckboxDefaults.colors(checkedColor = mainYellow)
                        )
                        Text(
                            text = "자동로그인",
                            color = mainGray
                        )
                    }
                }
            }

            // 하단부 버튼과 텍스트를 포함하는 Column
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 로그인 버튼
                MiddleButton(
                    text = "로그인",
                    onClick = {
                        // ViewModel에 로그인 요청 전달
                        loginViewModel.loginUser(id, password)

                        // 자동 로그인 상태 저장
                        if (isAutoLogin) {
                            editor.putString("saved_id", id)
                            editor.putString("saved_password", password)
                            editor.apply()
                        }
                    }
                )

                // 로그인 성공 시 화면 전환
                LaunchedEffect(loginResponse) {
                    if (loginResponse?.result?.code == 200) {
                        // JWT 토큰 저장
                        val token = loginResponse?.body?.accessToken
                        if (token != null) {
                            editor.putString("jwt_token", token)
                            editor.apply()
                            Log.d("LoginScreen", "JWT 토큰 저장 완료: $token")
                        }

                        navController.navigate(NavRoutes.Auth.Select.route) {
                            popUpTo(NavRoutes.Auth.route) { inclusive = true }
                        }
                    }
                }

                // 비밀번호 찾기 텍스트
                Text(
                    text = "비밀번호를 잊어버리셨나요?",
                    textAlign = TextAlign.Center,
                    color = mainGray,
                    modifier = Modifier
                        .clickable { navController.navigate("forgot_password") }
                )
            }
        }
    }
}



@Preview(
    name = "Login Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        navController = rememberNavController(),
        onLoginClick = {}
    )
}


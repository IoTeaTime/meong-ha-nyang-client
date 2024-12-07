package com.example.mhnfe.ui.screens.auth.login

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mhnfe.R
import com.example.mhnfe.di.UserType
import com.example.mhnfe.domain.mqtt.MqttViewModel
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.components.SendPasswordPopUp
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainYellow

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    mqttViewModel: MqttViewModel = hiltViewModel()
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

    // SharedPreferences를 사용해 사용자 정보를 저장
    val context = LocalContext.current

    val (dialogVisible, setDialogVisible) = remember { mutableStateOf(false) }

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
                .padding(horizontal = 34.dp, vertical = 20.dp),
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
                    modifier = modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )

                // 입력 필드들과 자동로그인을 포함하는 Column
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
                    horizontalAlignment = Alignment.Start
                ) {
                    MainTextBox(
                        focusManager = focusManager,
                        onIsErrorChange = { loginError = it },
                        inputText = id,
                        onInputTextChange = {
                            id = it
                            loginError = false
                        },
                        isError = loginError,
                        hintText = "아이디",
                        warningText = if (loginError) "아이디를 입력하세요." else ""
                    )

                    MainTextBox(
                        focusManager = focusManager,
                        onIsErrorChange = { passwordError = it },
                        inputText = password,
                        onInputTextChange = {
                            password = it
                            passwordError = false
                        },
                        isError = passwordError,
                        hintText = "비밀번호",
                        isPasswordField = true,
                        warningText = if (passwordError) "비밀번호를 입력하세요." else ""
                    )

                    // 자동 로그인 체크박스
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier
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

                if(dialogVisible) {
                    SendPasswordPopUp(
                        onConfirmation = {
                            setDialogVisible(false)
                        },
                        onDismissRequest = {
                            setDialogVisible(false)
                        }
                    )
                }
            }

            // 하단부 버튼과 텍스트를 포함하는 Column
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    14.dp,
                    alignment = Alignment.CenterVertically
                )
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                // 로그인 버튼
                MiddleButton(
                    text = "로그인",
                    onClick = {
                        if (id.isEmpty()) {
                            loginError = true
                        }
                        if (password.isEmpty()) {
                            passwordError = true
                        }
                        if (!loginError && !passwordError) {
                            // ViewModel에 로그인 요청 전달 (isAutoLogin 포함)
                            loginViewModel.loginUser(id, password, isAutoLogin)
                        }
                    }
                )

                // 로그인 성공 시 화면 전환
                LaunchedEffect(loginResponse) {
                    if (loginResponse?.result?.code == 200) {
                        mqttViewModel.initialize()
                        loginViewModel.getAccessToken { accessToken ->
                            // FCM 토큰 전송
                            if (!accessToken.isNullOrEmpty()) {
                                val sharedPref =
                                    context.getSharedPreferences("app_preferences", MODE_PRIVATE)
                                val fcmToken = sharedPref.getString("fcm_token", null)
                                Log.d("LoginScreen", "fcmToken: $fcmToken")

                                if (fcmToken != null) {
                                    try {
                                        loginViewModel.refreshFcmToken(accessToken, fcmToken)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error saving FCM token", e)
                                    }
                                } else {
                                    Log.e(TAG, "FCM 토큰이 저장되어 있지 않습니다.")
                                }
                            } else {
                                Log.e(TAG, "JWT 토큰이 null이어서 FCM 토큰 전송이 불가능합니다.")
                            }

                            Toast.makeText(context, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()

                            if(loginResponse?.body?.isGroupMember == true)
                            {
                                if(loginResponse?.body?.role== "ROLE_MASTER") {
                                    navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
                                        // Auth 플로우를 백스택에서 제거
                                        popUpTo(NavRoutes.Auth.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                                else{
                                    navController.navigate(NavRoutes.Main.createRoute(UserType.VIEWER)) {
                                        // Auth 플로우를 백스택에서 제거
                                        popUpTo(NavRoutes.Auth.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                            else {
                                navController.navigate(NavRoutes.Auth.Select.route) {
                                    popUpTo(NavRoutes.Auth.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                // 비밀번호 찾기 텍스트
                Text(
                    text = "비밀번호를 잊어버리셨나요?",
                    textAlign = TextAlign.Center,
                    color = mainGray,
                    modifier = modifier
                        .clickable {
                            setDialogVisible(true)
                        }
                )
            }
        }
    }
}
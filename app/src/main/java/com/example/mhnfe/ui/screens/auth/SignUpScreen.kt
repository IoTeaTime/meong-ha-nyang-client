package com.example.mhnfe.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.middleButton
import com.example.mhnfe.ui.theme.mainGray
import androidx.compose.ui.Alignment  // Import 추가
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import com.example.mhnfe.R
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,

    ) {
    val focusManager = LocalFocusManager.current
    var currentStep by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            SubTopBar(
                text = "회원가입",
                onBack = {
                    if (currentStep > 0) {
                        currentStep--
                    } else {
                        onBackClick()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 입력 필드들
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    0 -> {

                        Image(
                            painter = painterResource(id = R.drawable.dog_1),  // 이미지 리소스 ID
                            contentDescription = "아이디 입력 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)  // 이미지 높이 조절
                                .padding(vertical = 16.dp),
                            contentScale = ContentScale.Fit  // 이미지 스케일 타입
                        )
                        MainTextBox(
                            focusManager = focusManager,
                            hintText = "아이디를 입력해주세요"
                        )
                    }
                    1 -> {
                        Image(
                            painter = painterResource(id = R.drawable.dog_2),  // 이미지 리소스 ID
                            contentDescription = "아이디 입력 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)  // 이미지 높이 조절
                                .padding(vertical = 16.dp),
                            contentScale = ContentScale.Fit  // 이미지 스케일 타입
                        )
                        MainTextBox(
                            focusManager = focusManager,
                            hintText = "인증번호를 입력해주세요"
                        )
                    }
                    2 -> {
                        Image(
                            painter = painterResource(id = R.drawable.dog_3),  // 이미지 리소스 ID
                            contentDescription = "아이디 입력 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)  // 이미지 높이 조절
                                .padding(vertical = 16.dp),
                            contentScale = ContentScale.Fit  // 이미지 스케일 타입
                        )
                        MainTextBox(

                            focusManager = focusManager,
                            hintText = "비밀번호를 입력해주세요"
                        )
                        MainTextBox(
                            focusManager = focusManager,
                            hintText = "비밀번호 확인"
                        )
                    }
                    3 -> {
                        Image(
                            painter = painterResource(id = R.drawable.dog_4),  // 이미지 리소스 ID
                            contentDescription = "아이디 입력 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)  // 이미지 높이 조절
                                .padding(vertical = 16.dp),
                            contentScale = ContentScale.Fit  // 이미지 스케일 타입
                        )
                        MainTextBox(
                            focusManager = focusManager,
                            hintText = "닉네임을 입력해주세요"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 하단 버튼과 텍스트
            middleButton(
                text = if (currentStep == 3) "완료" else "다음",
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        onLoginClick()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (currentStep == 0) {
                Text(
                    text = "아이디가 있으신가요?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    color = mainGray
                )
            }

            // 하단 여백
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
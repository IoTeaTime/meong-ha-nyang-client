package com.example.mhnfe.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold

import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.middleButton
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainYellow

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isAutoLogin by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            SubTopBar(
                text = "로그인",
                onBack = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally  // 중앙 정렬 추가
        ) {
            // 입력 필드들
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MainTextBox(
                    focusManager = focusManager,
                    hintText = "아이디"
                )

                MainTextBox(
                    focusManager = focusManager,
                    hintText = "비밀번호"
                )

                // 자동 로그인 체크박스
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAutoLogin,
                        onCheckedChange = { isAutoLogin = it },
                        colors = CheckboxDefaults.colors(checkedColor = mainYellow)
                    )
                    Text("자동로그인")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 로그인 버튼
            middleButton(
                text = "로그인",
                onClick = onLoginClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // 비밀번호 찾기 텍스트
            Text(
                text = "비밀번호를 잊어버리셨나요?",
                modifier = Modifier
                    .padding(vertical = 16.dp),  // fillMaxWidth 제거
                textAlign = TextAlign.Center,
                color = mainGray
            )

            // 하단 여백
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
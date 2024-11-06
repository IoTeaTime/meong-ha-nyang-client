package com.example.mhnfe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.components.MiddleButton

@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 로고 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "명하냥 로고",
                modifier = Modifier.size(200.dp)
            )
        }

        // 버튼 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로그인 버튼
            MiddleButton(
                text = "로그인",
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Cam 회원 버튼
            MiddleButton(
                text = "회원가입",
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth()
            )

            // 소셜 로그인 버튼들
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao),
                    contentDescription = "카카오 로그인",
                    modifier = Modifier.size(40.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "구글 로그인",
                    modifier = Modifier.size(40.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.naver),
                    contentDescription = "네이버 로그인",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
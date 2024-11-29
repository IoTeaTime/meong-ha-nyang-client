package com.example.mhnfe.ui.screens.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.hoverYellow
import com.example.mhnfe.ui.theme.mainBlack

@Composable
fun PasswordEditScreen(
    modifier: Modifier = Modifier,
    bottomNavController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var textConfig by rememberSaveable { mutableStateOf("") }
    var textPW by rememberSaveable { mutableStateOf("") }
    var textPWComfirm by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val changePasswordResponse by profileViewModel.changeResponse.collectAsState()

    changePasswordResponse?.let {
        if (it.result.code == 200) {
            // 진입 경로에 따라 적절한 NavController에서 popBackStack 호출
            bottomNavController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            SubTopBar(
                text = "비밀번호 변경 페이지",
                onBack = {
                    bottomNavController.popBackStack()
                }
            )
        }
    ) {innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 34.dp, vertical = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(45.dp, alignment = Alignment.Top)
            ) {
                MainTextBox(
                    focusManager = focusManager,
                    inputText = textPW,
                    onInputTextChange = { newText ->
                        textPW = newText
                    },
                    hintText = "새 비밀번호"
                )
                MainTextBox(
                    focusManager = focusManager,
                    inputText = textPWComfirm,
                    onInputTextChange = { newText ->
                        textPWComfirm = newText
                    },
                    hintText = "새 비밀번호 확인"
                )
            }
            MiddleButton(
                text = "확인",
                onClick = {
                    profileViewModel.changePassword(textPW, textPWComfirm)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordEditPreview(
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()
    PasswordEditScreen(
        bottomNavController = navController
    )
}
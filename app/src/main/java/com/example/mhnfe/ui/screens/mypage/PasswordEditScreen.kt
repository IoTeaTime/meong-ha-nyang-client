package com.example.mhnfe.ui.screens.mypage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.MiddleButton

@Composable
fun PasswordEditScreen(
    modifier: Modifier = Modifier,
    bottomNavController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var currentPW by rememberSaveable { mutableStateOf("") }
    var newPW by rememberSaveable { mutableStateOf("") }
    var textPWComfirm by rememberSaveable { mutableStateOf("") }
    var passwordMismatch by rememberSaveable { mutableStateOf(false) }
    var incorrectCurrentPassword by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val changePasswordResponse by profileViewModel.changeResponse.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            profileViewModel.clearError()
        }
    }

    changePasswordResponse?.let {
        if (it.result.code == 200) {
            // 진입 경로에 따라 적절한 NavController에서 popBackStack 호출
            bottomNavController.popBackStack()
            Toast.makeText(context, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
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
                    inputText = currentPW,
                    onInputTextChange = { newText ->
                        currentPW = newText
                    },
                    isPasswordField = true,
                    hintText = "기존 비밀번호"
                )
                MainTextBox(
                    focusManager = focusManager,
                    inputText = newPW,
                    onInputTextChange = { newText ->
                        newPW = newText
                    },
                    isPasswordField = true,
                    hintText = "새 비밀번호"
                )
                MainTextBox(
                    focusManager = focusManager,
                    inputText = textPWComfirm,
                    onInputTextChange = { newText ->
                        textPWComfirm = newText
                    },
                    isPasswordField = true,
                    hintText = "새 비밀번호 확인",
                )
            }
            MiddleButton(
                text = "확인",
                onClick = {
                    passwordMismatch = newPW != textPWComfirm
                    if (passwordMismatch) {
                        Toast.makeText(context, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        profileViewModel.changePassword(currentPW, textPWComfirm)
                        incorrectCurrentPassword = false
                    }
                }
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun PasswordEditPreview(
//    modifier: Modifier = Modifier
//){
//    val navController = rememberNavController()
//    PasswordEditScreen(
//        bottomNavController = navController
//    )
//}
package com.example.mhnfe.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mhnfe.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import com.example.mhnfe.ui.theme.mainYellow

@Composable
fun SelectScreen(
    onBackClick: () -> Unit
) {
    var showQrDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }

    // 참여 QR AlertDialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("QR 생성") },
            text = { Text("참여 QR을 생성하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQrDialog = false
                        // 여기에 QR 생성 로직 추가
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQrDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 그룹 생성 AlertDialog
    if (showGroupDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("그룹 생성") },
            text = { Text("새로운 그룹을 생성하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGroupDialog = false
                        // 여기에 그룹 생성 로직 추가
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showGroupDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "로고",
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 32.dp)
                .size(200.dp),
            contentScale = ContentScale.Fit
        )

        // 버튼들
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 참여 QR 버튼
            Button(
                onClick = { showQrDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = mainYellow  // 메인 색상
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "참여 QR")
            }

            // 그룹 생성 버튼
            Button(
                onClick = { showGroupDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "그룹 생성")
            }
        }

        // 하단 여백
        Spacer(modifier = Modifier.height(48.dp))
    }
}

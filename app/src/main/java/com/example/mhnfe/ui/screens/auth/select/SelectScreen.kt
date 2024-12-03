package com.example.mhnfe.ui.screens.auth.select

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.theme.mainGray
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.Typography
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun SelectScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: SelectViewModel = hiltViewModel()
) {
    val groupState by viewModel.groupState.collectAsState()
    val navigateNext by viewModel.navigateNext.collectAsState()
    val error by viewModel.error.collectAsState()

    // 에러 메시지 표시를 위한 스낵바 상태
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(navigateNext) {
        if (navigateNext) {
            navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
                popUpTo(NavRoutes.Auth.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(text = "선택")
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 34.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 로고 이미지
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "로고",
                modifier = modifier
                    .padding(top = 80.dp)
                    .size(300.dp),
                contentScale = ContentScale.Fit
            )

            // 버튼들
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp, alignment = Alignment.CenterVertically)
            ) {
                // QR 스캔 버튼
                MiddleButton(
                    text = "참여 QR",
                    onClick = {
                        navController.navigate(NavRoutes.Auth.QRScanner.createRoute(UserType.VIEWER)) {
                            popUpTo(NavRoutes.Auth.Main.route) { inclusive = true }
                        }
                    },

                    )
                Button(
                    modifier = modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainGray
                    ),
                    onClick = {
                        viewModel.createGroup()
                    },
                ) {
                    Text(
                        style = Typography.labelLarge,
                        text = "그룹 생성",
                        color = Color.White
                    )
                }
            }
        }
    }
}
@Preview(
    name = "Select Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun SelectScreenPreview() {
    SelectScreen(
        navController = rememberNavController(),
    )
}
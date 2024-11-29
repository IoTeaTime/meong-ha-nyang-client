package com.example.mhnfe.ui.screens.mypage

import EditPopup
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainGray2
import deletePopup

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    nickname: String = "막내가짱이야",
    id: String = "nahaha",
    groupId: String = "IoTeatime",
    bottomNavController: NavController,
    mainNavController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val (dialogVisible, setDialogVisible) = remember { mutableStateOf(false) }
    val (dialogVisible1, setDialogVisible1) = remember { mutableStateOf(false) }

    val logoutResponse by profileViewModel.logoutResponse.collectAsState()
    val quitResponse by profileViewModel.quitResponse.collectAsState()

    logoutResponse?.let {
        if (it.result.code == 200) {
            mainNavController.navigate(NavRoutes.Auth.Main.route) {
                popUpTo(NavRoutes.Main.route) { inclusive = true }
            }
        }
    }

    quitResponse?.let {
        if (it.result.code == 200) {
            mainNavController.navigate(NavRoutes.Auth.Main.route) {
                popUpTo(NavRoutes.Main.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            MainTopBar(text = "마이페이지")
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(horizontal = 34.dp, vertical = 46.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ){
            Column (
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(35.dp, alignment = Alignment.CenterVertically)
            ){
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = mainGray2, shape = RoundedCornerShape(12.dp)),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp, alignment = Alignment.CenterHorizontally)
                    ) {
                        Image(
                            modifier = modifier.size(35.dp),
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "로고",
                            contentScale = ContentScale.Fit
                        )

                        Column(
                            modifier = modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = modifier,
                                    text = nickname,
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )

                                IconButton(
                                    modifier = modifier
                                        .size(22.dp),
                                    onClick = { setDialogVisible(true) }
                                ) {
                                    Icon(
                                        modifier = modifier.size(16.dp, 16.dp),
                                        painter = painterResource(id = R.drawable.edit),
                                        contentDescription = null,
                                        tint = Color.Unspecified
                                    )
                                }
                            }

                            Row(
                                modifier = modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(100.dp, alignment = Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = modifier,
                                    text = "ID",
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )
                                Text(
                                    modifier = modifier,
                                    text = id,
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )
                            }

                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.spacedBy(71.dp, alignment = Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = modifier,
                                    text = "그룹명",
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )
                                Text(
                                    modifier = modifier,
                                    text = groupId,
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )
                            }
                        }
                    }
                }

                if (dialogVisible) {
                    EditPopup(
                        onConfirmation = { setDialogVisible(false) },
                        onDismissRequest = { setDialogVisible(false) },
                        isDialogVisible = dialogVisible
                    )
                }

                if (dialogVisible1) {
                    deletePopup(
                        onConfirmation = {
                            profileViewModel.quit()
                            setDialogVisible1(false) },
                        onDismissRequest = { setDialogVisible1(false) },
                    )
                }

//                LaunchedEffect(quitResponse) {
//                    if(quitResponse?.result?.code == 200){
//                        mainNavController.navigate(NavRoutes.Auth.Main.route){
//                            popUpTo(NavRoutes.Main.route){ inclusive = true}
//                        }
//                    }
//                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = mainGray2, shape = RoundedCornerShape(12.dp))
                        .clickable(onClick = { bottomNavController.navigate("myPage/change_password") }),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "비밀번호 변경",
                            style = Typography.bodyMedium
                        )

                        Box(
                            modifier = modifier
                                .size(22.dp),
                        ) {
                            Icon(
                                modifier = modifier
                                    .size(16.dp, 16.dp)
                                    .align(Alignment.Center),
                                painter = painterResource(id = R.drawable.navigate_after),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = mainGray2, shape = RoundedCornerShape(12.dp))
                        .clickable(onClick = { bottomNavController.navigate("myPage/device_management") }),

                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "기기 관리",
                            style = Typography.bodyMedium
                        )

                        Box(
                            modifier = modifier
                                .size(22.dp),
                        ) {
                            Icon(
                                modifier = modifier
                                    .size(16.dp, 16.dp)
                                    .align(Alignment.Center),
                                painter = painterResource(id = R.drawable.navigate_after),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            }

            Column (
                modifier = modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(35.dp, alignment = Alignment.CenterVertically)
            ){
                TextButton(
                    onClick = {
                        profileViewModel.logout()

                    },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, mainBlack)
                ){
                    Text(
                        "로그아웃",
                        style = Typography.labelLarge,
                        color = mainBlack
                    )
                }

                TextButton(
                    onClick = { setDialogVisible1(true) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "회원탈퇴",
                        style = Typography.bodyMedium,
                        color = mainGray,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    val navController = rememberNavController()
    ProfileScreen(
        bottomNavController = navController,
        mainNavController = navController
    )
}
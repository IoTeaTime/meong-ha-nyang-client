package com.example.mhnfe.ui.screens.mypage

import EditPopup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mhnfe.R
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.components.LogoutPopUp
import com.example.mhnfe.ui.components.LongButton
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.screens.shared.AuthStateViewModel
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray2
import deletePopup

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userType: UserType,
    bottomNavController: NavController,
    mainNavController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authStateViewModel: AuthStateViewModel = hiltViewModel()
) {
    val (dialogVisible, setDialogVisible) = remember { mutableStateOf(false) }
    val (dialogVisible1, setDialogVisible1) = remember { mutableStateOf(false) }
    val (dialogVisible2, setDialogVisible2) = remember { mutableStateOf(false) }
    val (dialogVisible3, setDialogVisible3) = remember { mutableStateOf(false) }

    val logoutResponse by authStateViewModel.logoutResponse.collectAsState()
    val quitResponse by profileViewModel.quitResponse.collectAsState()
    val exitGroupResponse by profileViewModel.exitGroupResponse.collectAsState()

    val profileResponse by profileViewModel.profileResponse.collectAsState()
    val error by profileViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.fetchMemberDetails()
    }

    logoutResponse?.let {
        if (it.result.code == 200) {
            mainNavController.navigate(NavRoutes.Auth.Main.route) {
                popUpTo(NavRoutes.Main.route) { inclusive = true }
            }
        }
    }
    exitGroupResponse?.let {
        if (it.isSuccessful) {
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
            MainTopBar(
                text = "마이페이지",
                onImageClick = {
                    try {
                        authStateViewModel.logout()
                        mainNavController.navigate(NavRoutes.Auth.Main.route) {
                            popUpTo(NavRoutes.Main.route) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        mainNavController.navigate(NavRoutes.Auth.Main.route) {
                            popUpTo(NavRoutes.Main.route) { inclusive = true }
                        }
                    }
                })
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
            error?.let {
                Text(
                    text = it,
                    style = Typography.bodyMedium,
                    color = Color.Red
                )
            }

            Column (
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
            ) {
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
                        horizontalArrangement = Arrangement.spacedBy(
                            18.dp,
                            alignment = Alignment.CenterHorizontally
                        )
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
                            verticalArrangement = Arrangement.spacedBy(
                                10.dp,
                                alignment = Alignment.CenterVertically
                            ),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = modifier
                                        .wrapContentWidth()
                                        .wrapContentHeight(),
                                    horizontalArrangement = Arrangement.spacedBy(30.dp , alignment = Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = modifier,
                                        text = "닉네임",
                                        style = Typography.bodyMedium,
                                        color = mainBlack
                                    )
                                    profileResponse?.body?.member?.let {
                                        Text(
                                            modifier = modifier,
                                            text = it.nickname,
                                            style = Typography.bodyMedium,
                                            color = mainBlack
                                        )
                                    }
                                }

                                Spacer(modifier = modifier.weight(1f))

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
                                horizontalArrangement = Arrangement.spacedBy(30.dp , alignment = Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = modifier,
                                    text = "아이디",
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )
                                profileResponse?.body?.member?.id?.toString()?.let {
                                    Text(
                                        modifier = modifier,
                                        text = it,
                                        style = Typography.bodyMedium,
                                        color = mainBlack
                                    )
                                }
                            }

                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.spacedBy(30.dp , alignment = Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = modifier,
                                    text = "그룹명",
                                    style = Typography.bodyMedium,
                                    color = mainBlack
                                )
                                profileResponse?.body?.group?.groupName?.let {
                                    Text(
                                        modifier = modifier,
                                        text = it,
                                        style = Typography.bodyMedium,
                                        color = mainBlack
                                    )
                                }
                            }
                        }
                    }
                }
                LongButton(
                    text = "비밀번호 변경",
                    onClick = { bottomNavController.navigate("myPage/change_password") }
                )
                LongButton(
                    text = "그룹 나가기",
                    onClick = { setDialogVisible3(true) }
                )
                LongButton(
                    text = "회원 탈퇴",
                    onClick = { setDialogVisible1(true) }
                )
                if (userType == UserType.MASTER) {
                    LongButton(
                        text = "기기 관리",
                        onClick = { bottomNavController.navigate("myPage/device_management")}
                    )
                }
            }
            TextButton(
                onClick = {
                    setDialogVisible2(true)
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
            if (dialogVisible) {
                EditPopup(
                    onConfirmation = {
                        profileViewModel.fetchMemberDetails()
                        setDialogVisible(false)
                    },
                    onDismissRequest = { setDialogVisible(false) },
                    isDialogVisible = dialogVisible
                )
            }
            if (dialogVisible1) {
                deletePopup(
                    onConfirmation = {
                        profileViewModel.quit()
                        setDialogVisible1(false)
                    },
                    onDismissRequest = { setDialogVisible1(false) },
                )
            }

            if (dialogVisible2) {
                LogoutPopUp(
                    onConfirmation = {
                        authStateViewModel.logout()
                        setDialogVisible2(false)
                    },
                    text = "로그아웃 하시겠습니까?",
                    onDismissRequest = { setDialogVisible2(false) }
                )
            }
            if (dialogVisible3) {
                LogoutPopUp(
                    onConfirmation = {
                        profileViewModel.exitGroup()
                        setDialogVisible3(false)
                    },
                    text = "그룹을 나가시겠습니까?",
                    onDismissRequest = { setDialogVisible3(false) }
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun ProfileScreenPreview() {
//    val navController = rememberNavController()
//    ProfileScreen(
//        bottomNavController = navController,
//        mainNavController = navController
//    )
//}
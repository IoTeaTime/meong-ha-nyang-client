package com.example.mhnfe.ui.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.data.model.UserType
//import com.example.mhnfe.ui.screens.master.GroupScreen
//import com.example.mhnfe.ui.screens.master.HomeScreen
//import com.example.mhnfe.ui.screens.mypage.MyPageScreen
//import com.example.mhnfe.ui.screens.report.ReportScreen
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.hoverYellow
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainYellow

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Monitoring : NavigationItem("monitoring", R.drawable.monitoring, "모니터링")
    data object Report : NavigationItem("report", R.drawable.report, "리포트")
    data object MyPage : NavigationItem("myPage", R.drawable.mypage, "마이페이지")
}


@Composable
fun BottomBarScreen(
    mainNavController: NavController,
    userType: UserType  // 유저 타입 전달 받음
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        NavigationItem.Monitoring.route,
        NavigationItem.Report.route,
        NavigationItem.MyPage.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = bottomNavController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = NavigationItem.Monitoring.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Monitoring.route) {
//                GroupScreen(
//                    userType = userType,  // 전달받은 유저 타입 전달
//                    navController = mainNavController
//                )
            }
            composable(NavigationItem.Report.route) {
//                ReportScreen()
            }
            composable(NavigationItem.MyPage.route) {
//                MyPageScreen()
            }
        }
    }
}







@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(hoverYellow),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            NavigationItem.Monitoring,
            NavigationItem.Report,
            NavigationItem.MyPage,
        )

        items.forEach { item ->
            Box(
                modifier = modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (currentRoute == item.route) mainYellow else hoverYellow
                    )
                    .noRippleClickable {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = modifier.wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                        Icon(
                            modifier = modifier.size(30.dp),
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title,
                            tint = mainBlack
                        )
                    Text(
                        text = item.title,
                        style = Typography.labelSmall,
                        color = mainBlack
                    )
                }
            }
        }
    }
}

// 리플 효과 없는 클릭 modifier
fun Modifier.noRippleClickable(
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}


@Preview(showBackground = true)
@Composable
private fun BottomBarScreenPreview() {
    val context = LocalContext.current
    val navController = remember {
        NavController(context)
    }

    Column {
        BottomBarScreen(mainNavController = navController, userType = UserType.MASTER)
    }
}


package com.example.mhnfe.ui.screens.monitoring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.data.model.CCTV
import com.example.mhnfe.data.model.sampleCCTVList
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.components.SmallButton
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainGray3

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    groupId: String = "그룹1",
    userType: UserType = UserType.MASTER,
    //나중에 뷰모델로 뺄 것
    cctv: List<CCTV> = sampleCCTVList,
    navController: NavController
    ) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(text = groupId)
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(vertical = 28.dp, horizontal = 34.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically)
        ) {
            if (userType == UserType.MASTER) {
                Row(
                    modifier = modifier.fillMaxWidth().wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    SmallButton(
                        onClick = {
                            navController.navigate(
                                NavRoutes.Monitoring.QRGenerate.createRoute(UserType.CCTV)
                            )
                        },
                        text = "CCTV 추가"
                    )
                    SmallButton(
                        onClick = {
                            navController.navigate(
                                NavRoutes.Monitoring.QRGenerate.createRoute(UserType.VIEWER)
                            )
                        },
                        text = "참여자 추가"
                    )
                }
            }
            if (cctv.isEmpty()) {
                Text(style = Typography.bodyMedium, text = "등록된 CCTV가 없습니다.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = cctv,
                        key = { it.id }  // 각 아이템의 고유 키 설정
                    ) { cctvItem ->
                        CCTVItemCard(
                            cctv = cctvItem,
                        )
                    }
                }
            }
        }
    }
}

//예시로 만들어 놓은 것 다시만들어야함
@Composable
private fun CCTVItemCard(
    cctv: CCTV,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = mainGray3
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                    text = cctv.name,
                    style = Typography.bodyMedium
                )
                IconButton(
                    modifier = modifier.size(24.dp),
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "편집"
                    )
                }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GroupScreenPreview() {
    val navController = rememberNavController()
    GroupScreen(
        userType = UserType.VIEWER,
        navController = navController
    )
}
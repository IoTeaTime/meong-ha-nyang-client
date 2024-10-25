package com.example.mhnfe.ui.screens.master

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.ui.theme.Typography



@Composable
fun QRGenerateScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: QRViewModel = viewModel()
){
    //뒤로가기 설정
    BackHandler {
        navController.popBackStack()
    }
    //QR 내용
    val qrContent = viewModel.qrContent.value
    //QR 이미지 생성
    val qrBitmap = remember(qrContent) {
        viewModel.generateQRBitmap(500)
    }
    Column (
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Column (
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(120.dp, alignment = Alignment.CenterVertically)

        ){
            Text(
                textAlign = TextAlign.Center,
                style = Typography.bodyMedium,
                text = "CCTV로 사용할 기기에서\n QR 인증을 해주세요"
            )
            // QR 코드 이미지 표시
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(200.dp)
                    .border(1.dp, Color.Gray)
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
fun QRGeneratePreview(){
    val navController = rememberNavController()
    val viewModel: QRViewModel = viewModel()

    QRGenerateScreen(viewModel = viewModel, navController = navController)

}
package com.example.mhnfe.ui.screens.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MasterMainScreen(
    modifier: Modifier = Modifier,
    qrViewModel: QRViewModel = viewModel(),
    onQRButtonClick: () -> Unit
){
    Column (
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(
            onClick = {
                qrViewModel.generateNewQRCode()
                onQRButtonClick()
            }
        ) {
            Text(text = "QR 생성")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MasterPreview(){
    MasterMainScreen(onQRButtonClick = {})

}
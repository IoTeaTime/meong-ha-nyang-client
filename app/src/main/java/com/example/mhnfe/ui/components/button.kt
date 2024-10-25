package com.example.mhnfe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainYellow
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 18.dp, horizontal = 40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        onClick = onClick,
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(
            style = Typography.labelLarge,
            text = text,
            color = Color.Black)
    }
}

@Composable
fun middleButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 140.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = mainYellow
        ),
        onClick = onClick,
    ) {
        Text(
            style = Typography.labelLarge,
            text = text,
            color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun NewQuizPreview(){
    Column (
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
    ){
            SmallButton(text = "CCTV 추가") { }
            middleButton(text = "회원가입") { }
    }

}

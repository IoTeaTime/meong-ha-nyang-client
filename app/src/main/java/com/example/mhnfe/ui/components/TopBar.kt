package com.example.mhnfe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mhnfe.R
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainYellow

@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    text: String,
    onImageClick: () -> Unit = {}
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = mainYellow),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = modifier
                .padding(horizontal = 28.dp, vertical = 9.dp),
            text = text,
            style = Typography.titleMedium.copy(color = Color.White)
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(end = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                modifier = modifier
                    .size(35.dp, 35.dp)
                    .clickable { onImageClick() },
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "멍하냥 로고",
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun SubTopBar(
    modifier: Modifier = Modifier,
    text: String,
    onBack: () -> Unit = {}
){
    Column (
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ){
        Row(
            modifier = modifier
                .wrapContentSize()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(21.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(
                modifier = modifier
                    .size(30.dp),
                onClick = onBack
            ) {
                Icon(
                    modifier = modifier.size(9.dp, 15.dp),
                    painter = painterResource(id = R.drawable.navigate_before),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
            Text(
                modifier = modifier,
                text = text,
                style = Typography.titleMedium.copy(color = mainBlack)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainTopBarPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MainTopBar(text = "그룹 1")
        SubTopBar(text = "로그인")
    }
}
package com.example.mhnfe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.R

@Composable
fun LogoutPopUp(
    modifier: Modifier = Modifier,
    text:String,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = modifier.size(24.dp),
                        onClick = { onDismissRequest() }
                    ) {
                        Icon(
                            modifier = modifier.height(14.dp),
                            painter = painterResource(id = R.drawable.x),
                            contentDescription = null
                        )
                    }
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        color = mainBlack,
                        style = Typography.bodyMedium
                    )
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onConfirmation() },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, mainBlack)
                    ) {
                        Text(
                            "확인",
                            style = Typography.labelLarge,
                            color = mainBlack
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditPopupPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LogoutPopUp(onConfirmation = {},
            text = "로그아웃 하시겠습니까?",
            onDismissRequest = {}
        )
    }
}


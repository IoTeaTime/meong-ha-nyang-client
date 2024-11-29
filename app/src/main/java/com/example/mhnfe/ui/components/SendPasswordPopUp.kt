package com.example.mhnfe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.MainTextBox

@Composable
fun EditPopup(
    modifier: Modifier = Modifier,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val (email, onEmailChange) = remember { mutableStateOf("") }

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
                verticalArrangement = Arrangement.Center,
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
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "명하냥 로고",
                        modifier = modifier.size(50.dp, 50.dp)
                    )
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        modifier = modifier
                            .padding(PaddingValues(top = 15.dp)),
                        text = "임시 비밀번호 발급",
                        color = mainBlack,
                        style = Typography.bodyLarge
                    )
                }

                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        modifier = modifier.padding(PaddingValues(top = 15.dp)),
                        text = "임시 비밀번호를 발급받을 이메일을 입력해주세요",
                        color = mainBlack,
                        style = Typography.bodyMedium
                    )
                }

                Column(
                    modifier = modifier
                        .padding(PaddingValues(top = 30.dp, bottom = 36.dp))
                        .wrapContentSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MainTextBox(
                            modifier = modifier,
                            focusManager = focusManager,
                            inputText = email,
                            onInputTextChange = onEmailChange,
                        )
                    }
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
        EditPopup(onConfirmation = {},
            onDismissRequest = {}
        )
    }
}


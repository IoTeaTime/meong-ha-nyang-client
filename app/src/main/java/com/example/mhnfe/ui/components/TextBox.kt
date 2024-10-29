package com.example.mhnfe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainBlack

@Composable
fun MainTextBox(
    modifier: Modifier = Modifier,
    hintText: String = "" // hint text
) {
    var inputText by remember { mutableStateOf("") } // input text
    var isFocused by remember { mutableStateOf(false) } // focus state

    Box(
        modifier = modifier
            .width(340.dp)
            .height(39.dp)
            .border(
                color = if (isFocused) mainBlack else mainGray,
                width = if (isFocused) 3.dp else 1.dp,
                shape = RoundedCornerShape(4.dp)
            )
            .background(Color.White),
        contentAlignment = Alignment.CenterStart
    ) {
        // Show hintText before inputText
        if (inputText.isEmpty()) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = hintText,
                color = mainGray,
                style = Typography.bodyMedium
            )
        }

        BasicTextField(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp)
                // change color and style when focused
                .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
            textStyle = Typography.bodyMedium.copy(color = mainBlack),
            value = inputText,
            onValueChange = { newText -> inputText = newText }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainTextBoxPreview(){
    MainTextBox(hintText = "please input email")
}
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.R

@Composable
fun DialogWithImage(
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // 요소를 오른쪽으로 정렬
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.x),
                        contentDescription = null,
                        modifier = Modifier
                            .height(14.dp),
                        contentScale = ContentScale.Fit // 비율 유지
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.delete_dog),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth() // 너비를 가득 채우기
                        .height(84.dp), // 높이 설정
                    contentScale = ContentScale.Fit // 비율 유지
                )
                Text(
                    text = buildAnnotatedString {
                        append("회원탈퇴")
                        withStyle(style = SpanStyle(color = mainBlack)) {
                            append("를")
                        }
                    },
                    modifier = Modifier.padding(PaddingValues(top = 17.dp)),
                    color = Color.Red
                )
                Text(
                    text = "진행하시겠습니까?",
                    modifier = Modifier
                        .padding(PaddingValues(bottom = 30.dp)),
                    color = mainBlack
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
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
fun NewQuizPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DialogWithImage(onConfirmation = {}) { }
    }
}

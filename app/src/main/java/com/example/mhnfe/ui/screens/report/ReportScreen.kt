package com.example.mhnfe.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.data.remote.response.ImageInfo
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.screens.shared.AuthStateViewModel
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import java.time.LocalDate


@Composable
fun ReportDetailScreen(
    modifier: Modifier = Modifier,
    mainNavController: NavController,
    viewModel: ReportViewModel = hiltViewModel(),
    authStateViewModel: AuthStateViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedImage by remember { mutableStateOf<ImageInfo?>(null) }

    LaunchedEffect(selectedDate) {
        viewModel.loadImages(selectedDate)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(
                text = "리포트",
                onImageClick = {
                    try {
                        authStateViewModel.logout()
                        mainNavController.navigate(NavRoutes.Auth.Main.route) {
                            popUpTo(NavRoutes.Main.route) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        mainNavController.navigate(NavRoutes.Auth.Main.route) {
                            popUpTo(NavRoutes.Main.route) { inclusive = true }
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(vertical = 28.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Calendar(
                modifier = modifier
                    .padding(horizontal = 22.dp),
                initialDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                }
            )
            Column (
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(25.dp, alignment = Alignment.CenterVertically)
            ) {
                Text(
                    modifier = modifier
                        .padding(horizontal = 22.dp),
                    text = "하루 기록 🐾",
                    style = Typography.labelLarge,
                    color = mainBlack
                )
                if (uiState.images.isEmpty()) {
                    Box(
                        modifier = modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Text(
                            modifier = modifier.align(alignment = Alignment.Center),
                            text = "기록이 없습니다😢",
                            style = Typography.bodyMedium,
                            color = mainBlack
                        )
                    }

                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            alignment = Alignment.Start
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 22.dp),
                    ) {

                        items(
                            items = uiState.images,
                            key = { it.imageId }
                        ) { imageInfo ->
                            ReportItemCard(
                                onClick = { selectedImage = imageInfo },
                                date = imageInfo.formattedCreatedAt,
                                imagePath = imageInfo.imagePath
                            )
                        }
                    }
                    selectedImage?.let { imageInfo ->
                        ImageDialog(
                            imagePath = imageInfo.imagePath,
                            date = imageInfo.formattedCreatedAt,
                            onDismiss = { selectedImage = null }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun ImageDialog(
    imagePath: String,
    date: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp), // 최대 높이 제한
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 이미지
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // 닫기 버튼
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // 날짜
                Text(
                    text = date,
                    color = Color.White,
                    style = Typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
private fun Preview(){
    val navController = rememberNavController()
    ReportDetailScreen(
        mainNavController = navController
    )

}
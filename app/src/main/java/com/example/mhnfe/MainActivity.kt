package com.example.mhnfe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mhnfe.ui.navigation.MasterNavigation
import com.example.mhnfe.ui.theme.MhnFETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MhnFETheme {
                MasterNavigation()
            }
        }
    }
}


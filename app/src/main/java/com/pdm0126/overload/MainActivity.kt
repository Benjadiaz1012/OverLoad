package com.pdm0126.overload

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pdm0126.overload.ui.screens.InitTestScreen
import com.pdm0126.overload.ui.screens.SearchTestScreen
import com.pdm0126.overload.ui.theme.OverLoadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverLoadTheme {
                SearchTestScreen()
            }
        }
    }
}
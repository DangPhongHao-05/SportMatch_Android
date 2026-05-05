package com.example.sportmatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sportmatch.navigation.AppNavigation
import com.example.sportmatch.ui.theme.SportMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportMatchTheme {
                AppNavigation()
            }
        }
    }
}
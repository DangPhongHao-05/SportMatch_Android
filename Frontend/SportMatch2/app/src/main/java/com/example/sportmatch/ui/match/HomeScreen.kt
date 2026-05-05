package com.example.sportmatch.ui.match

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sportmatch.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Chào mừng Hào!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Đây là Trang chủ (Tìm sân - Tìm bạn)")

        Spacer(modifier = Modifier.height(32.dp))

        // Nút Đăng xuất để test điều hướng ngược lại
        Button(onClick = {
            navController.navigate(Screen.Login.route) {
                // Khi đăng xuất, xóa trang Home khỏi lịch sử
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }) {
            Text("Đăng xuất (Test)")
        }
    }
}
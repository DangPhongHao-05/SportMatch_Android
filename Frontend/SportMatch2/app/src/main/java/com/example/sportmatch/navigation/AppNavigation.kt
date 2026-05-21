package com.example.sportmatch.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportmatch.ui.auth.AuthViewModel
import com.example.sportmatch.ui.auth.LoginScreen
import com.example.sportmatch.ui.match.HomeScreen
import com.example.sportmatch.ui.match.MapScreen
import com.example.sportmatch.ui.notification.NotificationScreen

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    // Controller quản lý toàn bộ việc chuyển trang
    val navController = rememberNavController()

    // Khai báo NavHost, đặt trang bắt đầu (startDestination) là trang Login
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // Tuyến đường 1: Tới trang Login
        composable(Screen.Login.route) {
            // Truyền viewModel vào LoginScreen nếu màn hình này cần dùng
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Tuyến đường 2: Tới trang Home
        composable(Screen.Home.route) {
            HomeScreen(
                userName = authViewModel.userFullName,
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToMessages = { navController.navigate(Screen.Messages.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notification.route) }
            )
        }

        // 3. Màn hình Bản đồ (Map)
        composable(Screen.Map.route) {
            MapScreen(
                currentUserId = authViewModel.userId,
                onNavigateToBack = { navController.popBackStack() },
                onNavigateToChat = { hostId ->
                    // Luồng chat xử lý sau
                }
            )
        }

        // 4. Màn hình Tin nhắn (Messages)
        composable(Screen.Messages.route) {
            // Gọi màn hình danh sách Chat ở đây
        }

        // 5. Màn hình Hồ sơ (Profile)
        composable(Screen.Profile.route) {
            // Gọi màn hình thông tin cá nhân ở đây
        }

        // 6. Màn hình Thông báo (Notification)
        composable(Screen.Notification.route) {
            NotificationScreen(
                currentUserId = authViewModel.userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
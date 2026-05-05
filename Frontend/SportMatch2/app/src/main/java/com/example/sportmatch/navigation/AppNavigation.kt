package com.example.sportmatch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportmatch.ui.auth.LoginScreen
import com.example.sportmatch.ui.match.HomeScreen

@Composable
fun AppNavigation() {
    // Controller quản lý toàn bộ việc chuyển trang
    val navController = rememberNavController()

    // Khai báo NavHost, đặt trang bắt đầu (startDestination) là trang Login
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // Tuyến đường 1: Tới trang Login
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Tuyến đường 2: Tới trang Home
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}
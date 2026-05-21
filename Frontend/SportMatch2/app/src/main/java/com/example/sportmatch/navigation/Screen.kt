package com.example.sportmatch.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object Map : Screen("map_screen")
    object Messages : Screen("messages_screen")
    object Profile : Screen("profile_screen")
    object Notification : Screen("notification_screen")
}
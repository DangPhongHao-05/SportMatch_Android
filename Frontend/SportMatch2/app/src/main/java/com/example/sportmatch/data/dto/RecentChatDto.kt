package com.example.sportmatch.data.dto

data class RecentChatDto (
    val roomId: String = "",
    val users: List<String> = emptyList(), // Chứa ID của 2 người để dễ tìm
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val senderId: String = "",
    val isRead: Boolean = false
)
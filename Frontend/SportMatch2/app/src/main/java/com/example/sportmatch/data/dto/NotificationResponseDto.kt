package com.example.sportmatch.data.dto

data class NotificationResponseDto(
    val interactionId: Int,
    val matchRequestId: Int,
    val userId: Int,
    val senderName: String,
    val sportType: String,
    val message: String?,
    val status: String,
    val createdAt: String
)
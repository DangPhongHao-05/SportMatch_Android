package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class NotificationResponseDto(
    @SerializedName("id")
    val interactionId: Int,
    @SerializedName("matchRequestId")
    val matchRequestId: Int,
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("senderName")
    val senderName: String,
    @SerializedName("sportType")
    val sportType: String,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String
)
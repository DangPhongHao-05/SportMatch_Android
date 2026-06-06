package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

class MessageDto (
    @SerializedName("senderId")
    val senderId: String = "",
    @SerializedName("text")
    val text: String = "",
    @SerializedName("timestamp")
    val timestamp: Long = 0L
)
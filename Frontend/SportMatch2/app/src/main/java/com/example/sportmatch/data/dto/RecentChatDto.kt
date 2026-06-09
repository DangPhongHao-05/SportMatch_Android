package com.example.sportmatch.data.dto

import com.google.firebase.firestore.PropertyName

data class RecentChatDto(
    @get:PropertyName("roomId") @set:PropertyName("roomId")
    var roomId: String = "",

    @get:PropertyName("users") @set:PropertyName("users")
    var users: List<String> = emptyList(),

    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage")
    var lastMessage: String = "",

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = 0L,

    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: String = "",

    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false
)
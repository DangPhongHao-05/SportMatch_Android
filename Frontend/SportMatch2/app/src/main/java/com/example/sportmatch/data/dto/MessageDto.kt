package com.example.sportmatch.data.dto

import com.google.firebase.firestore.PropertyName

class MessageDto(
    @get:PropertyName("messageId") @set:PropertyName("messageId")
    var messageId: String = "",

    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: String = "",

    @get:PropertyName("text") @set:PropertyName("text")
    var text: String = "",

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = 0L,

    @get:PropertyName("fileUrl") @set:PropertyName("fileUrl")
    var fileUrl: String? = null,

    @get:PropertyName("kind") @set:PropertyName("kind")
    var kind: String? = "text"
)
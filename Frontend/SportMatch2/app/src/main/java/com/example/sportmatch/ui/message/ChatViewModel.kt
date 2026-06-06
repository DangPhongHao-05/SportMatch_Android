package com.example.sportmatch.ui.message

import androidx.lifecycle.ViewModel
import com.example.sportmatch.data.dto.MessageDto
import com.example.sportmatch.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    fun startListening(currentUserId: String, targetUserId: String) {
        repository.listenForMessages(currentUserId, targetUserId) { newMessages ->
            _messages.value = newMessages
        }
    }

    fun sendMessage(currentUserId: String, targetUserId: String, text: String) {
        if (text.isNotBlank()) {
            repository.sendMessage(currentUserId, targetUserId, text)
        }
    }
}
package com.example.sportmatch.ui.message

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.sportmatch.data.dto.RecentChatDto
import com.example.sportmatch.data.dto.UserDto
import com.example.sportmatch.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore

class MessageListViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _recentChats = MutableStateFlow<List<RecentChatDto>>(emptyList())
    val recentChats: StateFlow<List<RecentChatDto>> = _recentChats

    fun loadRecentChats(currentUserId: String) {
        repository.listenForRecentChats(currentUserId) { chats ->
            _recentChats.value = chats
        }
    }

    val userProfiles = mutableStateOf<Map<String, UserDto>>(emptyMap())

    fun fetchUserProfile(userId: String) {
        // 🟢 Thử ép kiểu userId về String chuẩn xác nhất có thể ở đây
        val docId = userId.trim()

        if (userProfiles.value.containsKey(docId)) return

        db.collection("Users").document(docId) // Dùng docId đã trim
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserDto::class.java)
                if (user != null) {
                    userProfiles.value = userProfiles.value + (docId to user)
                } else {
                    println("DEBUG: Không tìm thấy document với ID: $docId")
                }
            }
            .addOnFailureListener { e ->
                println("DEBUG: Lỗi load Firebase: ${e.message}")
            }
    }
}
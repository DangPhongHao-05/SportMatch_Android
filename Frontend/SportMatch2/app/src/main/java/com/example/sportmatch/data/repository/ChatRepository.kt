package com.example.sportmatch.data.repository

import com.example.sportmatch.data.dto.MessageDto
import com.example.sportmatch.data.dto.RecentChatDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()

    // Thuật toán tạo ID phòng chat duy nhất giữa 2 người
    private fun getRoomId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    // Gửi tin nhắn lên Firebase
    fun sendMessage(currentUserId: String, targetUserId: String, text: String) {
        val roomId = getRoomId(currentUserId, targetUserId)
        val timestamp = System.currentTimeMillis()

        val message = MessageDto(
            senderId = currentUserId,
            text = text,
            timestamp = timestamp
        )

        // Lưu vào chi tiết tin nhắn
        db.collection("Chats").document(roomId).collection("Messages").add(message)

        // Cập nhật ra ngoài Hộp thư (RecentChats)
        val recentChat = hashMapOf(
            "roomId" to roomId,
            "users" to listOf(currentUserId, targetUserId),
            "lastMessage" to text,
            "timestamp" to timestamp,
            "senderId" to currentUserId,
            "isRead" to false
        )
        // Dùng SetOptions.merge() để nếu phòng đã có thì nó ghi đè tin nhắn cuối, chưa có thì tạo mới
        db.collection("RecentChats").document(roomId).set(recentChat, SetOptions.merge())
    }

    // Lắng nghe tin nhắn mới 24/7
    fun listenForMessages(currentUserId: String, targetUserId: String, onUpdate: (List<MessageDto>) -> Unit) {
        val roomId = getRoomId(currentUserId, targetUserId)

        db.collection("Chats").document(roomId).collection("Messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MessageDto::class.java)
                } ?: emptyList()

                onUpdate(messages)
            }
    }

    // Lắng nghe danh sách Hộp thư
    fun listenForRecentChats(currentUserId: String, onUpdate: (List<RecentChatDto>) -> Unit) {
        db.collection("RecentChats")
            .whereArrayContains("users", currentUserId) // Tìm các phòng có mặt Hào
            .orderBy("timestamp", Query.Direction.DESCENDING) // Xếp tin nhắn mới nhất lên đầu
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val recentChats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RecentChatDto::class.java)
                } ?: emptyList()

                onUpdate(recentChats)
            }
    }

    fun markAsRead(roomId: String) {
        db.collection("RecentChats").document(roomId)
            .update("isRead", true)
    }
}
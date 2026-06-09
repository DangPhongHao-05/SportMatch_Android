package com.example.sportmatch.data.repository

import android.util.Log
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
//    fun sendMessage(currentUserId: String, targetUserId: String, text: String) {
//        val roomId = getRoomId(currentUserId, targetUserId)
//        val timestamp = System.currentTimeMillis()
//
//        val message = MessageDto(
//            senderId = currentUserId,
//            text = text,
//            timestamp = timestamp
//        )
//
//        // Lưu vào chi tiết tin nhắn
//        db.collection("Chats").document(roomId).collection("Messages").add(message)
//
//        // Cập nhật ra ngoài Hộp thư (RecentChats)
//        val recentChat = hashMapOf(
//            "roomId" to roomId,
//            "users" to listOf(currentUserId, targetUserId),
//            "lastMessage" to text,
//            "timestamp" to timestamp,
//            "senderId" to currentUserId,
//            "isRead" to false
//        )
//        // Dùng SetOptions.merge() để nếu phòng đã có thì nó ghi đè tin nhắn cuối, chưa có thì tạo mới
//        db.collection("RecentChats").document(roomId).set(recentChat, SetOptions.merge())
//    }
    fun sendMessage(currentUserId: String, targetUserId: String, messageData: Map<String, Any>) {
        val roomId = getRoomId(currentUserId, targetUserId)

        // Lưu vào chi tiết tin nhắn
        db.collection("rooms").document(roomId)
            .collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                // Sau khi lưu chi tiết thành công, cập nhật Hộp thư
                updateRecentChat(currentUserId, targetUserId, messageData)
            }
            .addOnFailureListener { e ->
                Log.e("ChatRepository", "Lỗi gửi tin nhắn: ${e.message}")
            }
    }

    // Lắng nghe tin nhắn mới 24/7
    fun listenForMessages(currentUserId: String, targetUserId: String, onUpdate: (List<MessageDto>) -> Unit) {
        val roomId = getRoomId(currentUserId, targetUserId)

        db.collection("rooms").document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Lỗi lắng nghe messages: ${error.message}")
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.map { doc ->
                    MessageDto(
                        messageId = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        fileUrl = doc.getString("fileUrl"),
                        kind = doc.getString("kind") ?: "text"
                    )
                } ?: emptyList()

                onUpdate(messages)
            }
    }

    // Lắng nghe danh sách Hộp thư
    fun listenForRecentChats(currentUserId: String, onUpdate: (List<RecentChatDto>) -> Unit) {
        db.collection("RecentChats")
            .whereArrayContains("users", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("CHAT_REPO", "Lỗi lắng nghe: ${error.message}")
                    return@addSnapshotListener
                }

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

    fun updateRecentChat(currentUserId: String, targetUserId: String, messageData: Map<String, Any>) {
        val roomId = getRoomId(currentUserId, targetUserId)

        val recentChat = hashMapOf(
            "roomId" to roomId,
            "users" to listOf(currentUserId, targetUserId),
            "lastMessage" to (messageData["text"] ?: ""),
            "timestamp" to (messageData["timestamp"] ?: System.currentTimeMillis()),
            "senderId" to currentUserId,
            "isRead" to false
        )

        db.collection("RecentChats").document(roomId)
            .set(recentChat, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("ChatRepository", "Lỗi update RecentChat: ${e.message}")
            }
    }

    // Xóa tin nhắn
    fun deleteMessage(currentUserId: String, targetUserId: String, messageId: String) {
        val roomId = getRoomId(currentUserId, targetUserId)
        db.collection("rooms").document(roomId).collection("messages").document(messageId).delete()
    }

    // Sửa tin nhắn
    fun editMessage(currentUserId: String, targetUserId: String, messageId: String, newText: String) {
        val roomId = getRoomId(currentUserId, targetUserId)
        db.collection("rooms").document(roomId).collection("messages").document(messageId)
            .update("text", newText)
    }
}
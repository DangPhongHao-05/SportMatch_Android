package com.example.sportmatch.ui.message

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.sportmatch.data.dto.MessageDto
import com.example.sportmatch.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

//    fun startListening(currentUserId: String, targetUserId: String) {
//        repository.listenForMessages(currentUserId, targetUserId) { newMessages ->
//            Log.d("CheckFlow", "Số lượng tin nhắn nhận được: ${newMessages.size}")
//            _messages.value = newMessages
//        }
//    }

    fun startListening(currentUserId: String, targetUserId: String) {
        repository.listenForMessages(currentUserId, targetUserId) { newMessages ->
            _messages.value = newMessages
        }
    }
//    fun startListening(currentUserId: String, targetUserId: String) {
//        val roomId = if (currentUserId < targetUserId) "${currentUserId}_${targetUserId}" else "${targetUserId}_${currentUserId}"
//
//        FirebaseFirestore.getInstance().collection("rooms").document(roomId)
//            .collection("messages")
//            .orderBy("timestamp", Query.Direction.ASCENDING)
//            .addSnapshotListener { snapshot, _ ->
//                val list = snapshot?.documents?.map { doc ->
//                    MessageDto(
//                        messageId = doc.id,
//                        senderId = doc.getString("senderId") ?: "",
//                        text = doc.getString("text") ?: "",
//                        timestamp = doc.getLong("timestamp") ?: 0L,
//                        fileUrl = doc.getString("fileUrl"),
//                        kind = doc.getString("kind") ?: "text" // Mặc định là 'text' cho các tin nhắn cũ
//                    )
//                } ?: emptyList()
//                _messages.value = list
//            }
//    }

//    fun sendMessage(currentUserId: String, targetUserId: String, text: String) {
//        if (text.isNotBlank()) {
//            repository.sendMessage(currentUserId, targetUserId, text)
//        }
//    }

//    fun sendMessage(currentUserId: String, targetUserId: String, text: String) {
//        if (text.isNotBlank()) {
//            val roomId = if (currentUserId < targetUserId) "${currentUserId}_${targetUserId}" else "${targetUserId}_${currentUserId}"
//
//            val messageData = mapOf<String, Any>(
//                "text" to text,
//                "kind" to "text",
//                "senderId" to currentUserId,
//                "timestamp" to System.currentTimeMillis()
//            )
//
//            FirebaseFirestore.getInstance().collection("rooms").document(roomId)
//                .collection("messages").add(messageData)
//                .addOnSuccessListener {
//                    // Cập nhật RecentChats sau khi gửi thành công
//                    repository.updateRecentChat(currentUserId, targetUserId, messageData)
//                }
//        }
//    }
    fun sendMessage(currentUserId: String, targetUserId: String, text: String) {
        if (text.isNotBlank()) {
            val messageData = mapOf<String, Any>(
                "text" to text,
                "kind" to "text",
                "senderId" to currentUserId,
                "timestamp" to System.currentTimeMillis()
            )
            repository.sendMessage(currentUserId, targetUserId, messageData)
        }
    }


    // Thêm tham số currentUserId vào hàm này
    // 🟢 Upload ảnh và gửi qua Repository
    fun uploadFileToFirebase(uri: Uri, type: String, currentUserId: String, targetUserId: String) {
        val fileExtension = if (type == "image") ".jpg" else ".pdf"
        val fileName = "${UUID.randomUUID()}$fileExtension"
        val storageRef = FirebaseStorage.getInstance().reference.child("chats/$fileName")

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val messageData = mapOf<String, Any>(
                    "text" to if (type == "image") "[Ảnh]" else "[Tệp]",
                    "fileUrl" to downloadUrl.toString(),
                    "kind" to if (type == "image") "picture" else "file",
                    "senderId" to currentUserId,
                    "timestamp" to System.currentTimeMillis()
                )
                // Gọi repository để gửi
                repository.sendMessage(currentUserId, targetUserId, messageData)
            }
        }.addOnFailureListener { e ->
            Log.e("CheckFlow", "Lỗi upload file: ${e.message}")
        }
    }

    // 🟢 Xóa tin nhắn
    fun deleteMessage(currentUserId: String, targetUserId: String, messageId: String) {
        repository.deleteMessage(currentUserId, targetUserId, messageId)
    }

    // 🟢 Sửa tin nhắn
    fun editMessage(currentUserId: String, targetUserId: String, messageId: String, newText: String) {
        if (newText.isNotBlank()) {
            repository.editMessage(currentUserId, targetUserId, messageId, newText)
        }
    }
}
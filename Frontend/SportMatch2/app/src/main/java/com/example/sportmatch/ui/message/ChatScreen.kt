package com.example.sportmatch.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportmatch.data.repository.ChatRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    targetUserId: String,
    targetUserName: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.startListening(currentUserId, targetUserId)
        // Cập nhật trạng thái đọc
        val roomId = if (currentUserId < targetUserId) "${currentUserId}_${targetUserId}" else "${targetUserId}_${currentUserId}"
        ChatRepository().markAsRead(roomId)
    }

    // CẤU TRÚC ĐÚNG: Column bao ngoài, không dùng Scaffold để tránh bị hệ thống ép padding
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        // 1. THANH TOP BAR
        TopAppBar(
            title = { Text(targetUserName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) // Dòng này ép sát mép
        )
        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

        // 2. DANH SÁCH TIN NHẮN (Dùng weight(1f) để ép nó dãn ra)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            items(messages) { msg ->
                MessageBubble(text = msg.text, isMe = msg.senderId == currentUserId)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // 3. THANH NHẬP TIN NHẮN (Dán đáy, không bị Scaffold che)
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập tin nhắn...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage(currentUserId, targetUserId, inputText)
                        inputText = ""
                    },
                    modifier = Modifier.size(48.dp).background(Color(0xFF2196F3), CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isMe: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isMe) Color.White else Color.Black,
            fontSize = 15.sp,
            modifier = Modifier
                .background(
                    color = if (isMe) Color(0xFF2196F3) else Color.White,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
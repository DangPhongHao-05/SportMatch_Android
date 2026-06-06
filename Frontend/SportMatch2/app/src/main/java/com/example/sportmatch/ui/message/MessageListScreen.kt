package com.example.sportmatch.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.sportmatch.data.dto.RecentChatDto
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChatDetail: (String, String) -> Unit,
    viewModel: MessageListViewModel = viewModel()
) {
    val recentChats by viewModel.recentChats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRecentChats(currentUserId)
    }

    // 1. Dùng Column bao ngoài thay vì Scaffold để tự chủ hoàn toàn layout
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // 2. Tái sử dụng phong cách của NotificationScreen
        Column {
            TopAppBar(
                title = { Text("Đoạn chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        }

        // 3. Nội dung danh sách
        if (recentChats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có cuộc trò chuyện nào", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(recentChats) { chat ->
                    val targetUserId = chat.users.firstOrNull { it != currentUserId } ?: ""

                    LaunchedEffect(targetUserId) { viewModel.fetchUserProfile(targetUserId) }
                    val userProfile = viewModel.userProfiles.value[targetUserId]
                    val displayName = userProfile?.fullName ?: "Đang tải..."

                    ChatListItem(
                        chat = chat,
                        targetUserId = targetUserId,
                        currentUserId = currentUserId,
                        targetUserName = displayName, // Truyền tên thật
                        onClick = { id, name -> onNavigateToChatDetail(id, name) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: RecentChatDto,
    targetUserId: String,
    currentUserId: String,
    targetUserName: String,
    onClick: (String, String) -> Unit
) {
    val isMe = chat.senderId == currentUserId
    val prefix = if (isMe) "Bạn: " else ""

    // Logic in đậm nếu chưa đọc
    val fontWeight = if (chat.isRead) FontWeight.Normal else FontWeight.Bold
    val textColor = if (chat.isRead) Color.Gray else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(targetUserId, targetUserName) })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = targetUserName.take(1).uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Nội dung tin nhắn
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = targetUserName,
                fontWeight = if (chat.isRead) FontWeight.Bold else FontWeight.ExtraBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$prefix${chat.lastMessage}",
                fontWeight = fontWeight,
                color = textColor,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Thời gian
        Text(
            text = formatTime(chat.timestamp),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

// Hàm format timestamp mili-giây sang giờ phút
fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
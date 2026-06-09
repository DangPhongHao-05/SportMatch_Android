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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

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
                    val displayAvatar = userProfile?.avatarUrl
                    val isUnreadForMe = !chat.isRead && chat.senderId != currentUserId

                    ChatListItem(
                        chat = chat,
                        targetUserId = targetUserId,
                        currentUserId = currentUserId,
                        targetUserName = displayName,
                        targetUserAvatar = displayAvatar,
                        isUnread = isUnreadForMe,
                        onClick = { id, name ->
                            // đánh dấu đọc lên Firebase
                            if (chat.senderId != currentUserId) {
                                viewModel.markChatAsRead(chat.roomId)
                            }
                            onNavigateToChatDetail(id, name)
                        }
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
    targetUserAvatar: String?,
    isUnread: Boolean,
    onClick: (String, String) -> Unit
) {
    val isMe = chat.senderId == currentUserId
    val prefix = if (isMe) "Bạn: " else ""
    val isUnreadForMe = !chat.isRead && !isMe

    // Logic in đậm nếu chưa đọc
    val fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isUnread) Color.Black else Color.Gray
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(targetUserId, targetUserName) })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (!targetUserAvatar.isNullOrBlank()) {
            AsyncImage(
                model = targetUserAvatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        } else {
            // Fallback khi không có ảnh
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                val initialChar = if (targetUserName.isNotBlank() && targetUserName != "Đang tải...") {
                    targetUserName.take(1).uppercase()
                } else "?"
                Text(
                    text = initialChar,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Nội dung tin nhắn
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = targetUserName,
                fontWeight = if (isUnreadForMe) FontWeight.ExtraBold else FontWeight.Bold,
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
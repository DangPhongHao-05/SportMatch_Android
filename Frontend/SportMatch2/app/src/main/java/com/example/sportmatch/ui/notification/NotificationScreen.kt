package com.example.sportmatch.ui.notification

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportmatch.data.dto.MyRequestDto
import com.example.sportmatch.data.dto.NotificationResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    currentUserId: Int,
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Chờ mình duyệt", "Đơn mình gửi")

    // Kích hoạt gọi API khi mở màn hình hoặc chuyển Tab
    LaunchedEffect(currentUserId, selectedTabIndex) {
        if (selectedTabIndex == 0) {
            viewModel.fetchNotifications(hostId = currentUserId)
        } else {
            viewModel.fetchMyRequests(userId = currentUserId)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Yêu cầu tham gia", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                )
                // --- THANH CHUYỂN TAB ---
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFF2196F3)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) Color(0xFF2196F3) else Color.Gray
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // TAB 1: CHỜ MÌNH DUYỆT
            if (selectedTabIndex == 0) {
                if (viewModel.notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có yêu cầu nào đang chờ duyệt", color = Color.Gray, fontSize = 15.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        items(viewModel.notifications) { notif ->
                            NotificationCard(
                                notif = notif,
                                onAccept = {
                                    viewModel.respondToRequest(notif.interactionId, isAccepted = true) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onReject = {
                                    viewModel.respondToRequest(notif.interactionId, isAccepted = false) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            // TAB 2: ĐƠN MÌNH GỬI
            else {
                if (viewModel.myRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, contentDescription = "", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Bạn chưa gửi yêu cầu tham gia nào", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        items(viewModel.myRequests) { request ->
                            MyRequestCard(
                                request = request,
                                onCancel = {
                                    viewModel.cancelMyRequest(request.id, currentUserId) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// CARD TAB 1: hiển thị danh sách thông báo chờ duyêt
@Composable
fun NotificationCard(
    notif: NotificationResponseDto,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFE3F2FD), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Person, "", tint = Color(0xFF2196F3), modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = notif.senderName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Muốn tham gia kèo ${notif.sportType}", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (notif.message.isNullOrBlank()) "\"Người này không để lại lời nhắn.\"" else "\"${notif.message}\"",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (notif.status) {
                "Pending" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onReject,
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE), contentColor = Color.DarkGray),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Close, "", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Từ chối")
                        }

                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, "", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đồng ý")
                        }
                    }
                }
                "Accepted" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bạn đã chấp nhận yêu cầu này",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                "Rejected" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bạn đã từ chối yêu cầu này",
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


// CARD TAB 2: hiển thị danh sách yêu cầu của mình
@Composable
fun MyRequestCard(
    request: MyRequestDto,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFFFF3E0), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SportsSoccer, "", tint = Color(0xFFFF9800), modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Yêu cầu tham gia: ${request.sportType}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Người tổ chức: ${request.hostName}", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (request.status) {
                "Pending" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("⏳ Đang chờ duyệt...", color = Color(0xFFFF9800), fontWeight = FontWeight.Medium)
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Hủy yêu cầu", fontSize = 13.sp)
                        }
                    }
                }
                "Accepted" -> {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                        Text("Đã được chấp nhận!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                "Rejected" -> {
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
                        Text("Đã bị từ chối!", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
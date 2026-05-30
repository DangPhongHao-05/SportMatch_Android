package com.example.sportmatch.ui.match

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onNavigateToMap: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    // HIỆU ỨNG SÓNG LAN TỎA (PULSE GLOW) CHO NÚT ĐỊNH VỊ
    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")

    // Tạo hiệu ứng vòng tròn sóng to dần từ 100% lên 150%
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlowScale"
    )

    // Tạo hiệu ứng mờ dần (Alpha) khi sóng tỏa ra xa
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlowAlpha"
    )

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(28.dp), clip = false),
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { /* Home */ }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                            Text("Trang chủ", fontSize = 9.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }

                    IconButton(onClick = onNavigateToMessages, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Email, contentDescription = "Messages", tint = Color.Gray, modifier = Modifier.size(24.dp))
                            Text("Tin nhắn", fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                        }
                    }

                    // Spacer này vẫn giữ nguyên để chừa chỗ cho FAB
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = onNavigateToNotifications, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Gray, modifier = Modifier.size(24.dp))
                            Text("Thông báo", fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                        }
                    }

                    IconButton(onClick = onNavigateToProfile, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.Gray, modifier = Modifier.size(24.dp))
                            Text("Hồ sơ", fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // CỤM NÚT MAP NHẤP NHÁY NỔI BẬT
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = 70.dp) // Đẩy ghim dịch nhẹ xuống ăn khớp vào khe trũng BottomBar
            ) {
                // Lớp 1: Vòng tròn hiệu ứng sóng phát sáng (Pulsing Effect) chạy ngầm phía sau
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer(
                            scaleX = glowScale,
                            scaleY = glowScale
                        )
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = glowAlpha),
                            shape = CircleShape
                        )
                )

                // Lớp 2: Nút bấm chính với dải màu Gradient chuyển sắc mượt mà
                FloatingActionButton(
                    onClick = onNavigateToMap,
                    containerColor = Color.Transparent, // Tắt màu mặc định để tự đổ màu Gradient bên dưới
                    elevation = FloatingActionButtonDefaults.elevation(0.dp), // Tắt shadow thô của FAB gốc
                    shape = CircleShape,
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF66BB6A), Color(0xFF4CAF50)) // Xanh lá đổ từ nhạt sang đậm
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Map",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        // GIAO DIỆN NỘI DUNG TRANG CHỦ
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Header hiển thị tên User động
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Chào mừng trở lại,", fontSize = 16.sp, color = Color.Gray)
                    Text(
                        text = "$userName",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFF2196F3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = userName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Banner chính gợi ý quét map
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier.fillMaxWidth().height(150.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Sẵn sàng ra sân chưa?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tìm các trận đấu quanh bạn ngay lập tức!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToMap,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Quét bản đồ ngay",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(text = "Môn thể thao phổ biến", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            Row(modifier = Modifier.padding(top = 10.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 20.dp)) {
                    Box(modifier = Modifier.size(60.dp).background(Color.White, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color(0xFF4CAF50)) // Sửa sang Icon quả bóng thật cho đẹp
                    }
                    Text(text = "Bóng đá", fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 20.dp)) {
                    Box(modifier = Modifier.size(60.dp).background(Color.White, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9800))
                    }
                    Text(text = "Cầu lông", fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}
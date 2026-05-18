package com.example.sportmatch.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Biến lưu trữ thời gian đếm ngược (60 giây)
    var timeLeft by remember { mutableIntStateOf(60) }

    val context = LocalContext.current
    val activity = context as? Activity

    // Xử lý tự động dọn dẹp Text và Reset thời gian
    LaunchedEffect(uiState) {
        if (uiState == LoginState.OTP_INPUT) {
            inputText = ""
            timeLeft = 60 // Reset về 60s mỗi khi vào màn nhập OTP
        } else if (uiState == LoginState.PHONE_INPUT) {
            inputText = ""
        }
    }

    // Logic đếm ngược thời gian
    LaunchedEffect(timeLeft, uiState) {
        if (uiState == LoginState.OTP_INPUT && timeLeft > 0) {
            delay(1000L) // Đợi 1 giây
            timeLeft--   // Giảm đi 1
        }
    }

    // NỀN TỔNG THỂ: Sử dụng màu xám nhạt 0xFFF5F5F5 đồng bộ với HomeScreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- PHẦN 1: THIẾT KẾ LOGO ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF66BB6A), Color(0xFF4CAF50))
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SPM App",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2196F3),
                        lineHeight = 32.sp
                    )
                    Text(
                        text = "Bản đồ thể thao",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- PHẦN 2: CARD NỀN TRẮNG CHỨA FORM CHUYỂN TRẠNG THÁI (BO GÓC 24.DP) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, shape = RoundedCornerShape(24.dp), clip = false),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = uiState,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "LoginTransition"
                    ) { state ->
                        when (state) {
                            LoginState.PHONE_INPUT -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Đăng nhập hoặc Đăng ký",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Vui lòng nhập số điện thoại để tiếp tục",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(28.dp))

                                    // Custom Ô nhập SĐT chuẩn Material 3 bo tròn mềm mại
                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        label = { Text("Số điện thoại") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2196F3)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF2196F3),
                                            focusedLabelColor = Color(0xFF2196F3)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Nút Tiếp tục bản to bọc dải màu Gradient xịn sò
                                    Button(
                                        onClick = {
                                            if (activity != null && inputText.isNotBlank()) {
                                                viewModel.onPhoneSubmit(inputText, activity)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2196F3) // Đồng bộ màu thương hiệu chính
                                        ),
                                        enabled = inputText.length >= 10
                                    ) {
                                        Text("Tiếp tục", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            LoginState.OTP_INPUT -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Xác thực mã OTP",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Mã 6 số đã được gửi đến số:",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = viewModel.phoneNumber,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.height(28.dp))

                                    // Cụm 6 ô vuông OTP đã được chuốt bo góc cực đẹp
                                    OtpInputField(
                                        otpText = inputText,
                                        onOtpTextChange = {
                                            inputText = it
                                            if (it.length == 6) {
                                                viewModel.onOtpSubmit(it)
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(28.dp))

                                    Button(
                                        onClick = { viewModel.onOtpSubmit(inputText) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Đổi sang màu xanh lá cây khi xác nhận thành công giống nút ghim
                                        enabled = inputText.length == 6
                                    ) {
                                        Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // --- PHẦN NÚT GỬI LẠI MÃ & ĐẾM NGƯỢC ---
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "Chưa nhận được mã? ", color = Color.Gray, fontSize = 14.sp)
                                        TextButton(
                                            onClick = {
                                                if (activity != null) {
                                                    viewModel.onPhoneSubmit(viewModel.phoneNumber, activity)
                                                    timeLeft = 60
                                                }
                                            },
                                            enabled = timeLeft == 0
                                        ) {
                                            Text(
                                                text = if (timeLeft > 0) "Gửi lại sau ${timeLeft}s" else "Gửi lại ngay",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (timeLeft > 0) Color.Gray else Color(0xFF2196F3)
                                            )
                                        }
                                    }
                                }
                            }

                            LoginState.LOADING -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 20.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = Color(0xFF2196F3))
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text("Đang xử lý...", color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            LoginState.SUCCESS -> {
                                LaunchedEffect(Unit) {
                                    onLoginSuccess()
                                }
                            }

                            LoginState.ERROR -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFEBEE),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Error",
                                            tint = Color(0xFFE91E63),
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Xác thực thất bại",
                                        fontSize = 20.sp,
                                        color = Color(0xFFE91E63),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Mã OTP không đúng hoặc đã hết hạn.",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(28.dp))

                                    Button(
                                        onClick = { viewModel.resetToOtpState() },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                    ) {
                                        Text("Thử lại OTP", fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = { viewModel.resetToPhoneState() },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                                    ) {
                                        Text("Đổi số điện thoại khác", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =================================================================
// COMPOSABLE TẠO 6 Ô VUÔNG NHẬP OTP (ĐÃ ĐƯỢC CHUỐT VIP HƠN)
// =================================================================
@Composable
fun OtpInputField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6,
    onOtpTextChange: (String) -> Unit
) {
    BasicTextField(
        modifier = modifier,
        value = otpText,
        onValueChange = {
            if (it.length <= otpCount && it.all { char -> char.isDigit() }) {
                onOtpTextChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Tạo khoảng cách đều nhau mượt mà giữa các khối
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpCount) { index ->
                    val isFocused = otpText.length == index
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f) // Tự động chia đều kích thước hộp theo màn hình điện thoại
                            .height(54.dp)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp) // Bo góc đồng bộ 12.dp
                            )
                            .background(
                                color = if (isFocused) Color(0xFF2196F3).copy(alpha = 0.06f) else Color.White,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    )
}
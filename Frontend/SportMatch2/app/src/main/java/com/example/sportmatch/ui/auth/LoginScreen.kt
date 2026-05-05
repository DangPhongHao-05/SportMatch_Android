package com.example.sportmatch.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportmatch.navigation.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    navController: NavController,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SportMatch",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
            },
            label = "LoginTransition"
        ) { state ->
            when (state) {
                LoginState.PHONE_INPUT -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Đăng nhập hoặc Đăng ký",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vui lòng nhập số điện thoại để tiếp tục",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = { Text("Số điện thoại") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (activity != null && inputText.isNotBlank()) {
                                    viewModel.onPhoneSubmit(inputText, activity)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = inputText.length >= 10
                        ) {
                            Text("Tiếp tục", fontSize = 16.sp)
                        }
                    }
                }

                LoginState.OTP_INPUT -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Xác thực OTP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mã 6 số đã được gửi đến số",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = viewModel.phoneNumber,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        OtpInputField(
                            otpText = inputText,
                            onOtpTextChange = {
                                inputText = it
                                if (it.length == 6) {
                                    viewModel.onOtpSubmit(it)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.onOtpSubmit(inputText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = inputText.length == 6
                        ) {
                            Text("Xác nhận", fontSize = 16.sp)
                        }

                        // --- PHẦN NÚT GỬI LẠI MÃ & ĐẾM NGƯỢC ---
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Chưa nhận được mã? ", color = Color.Gray)
                            TextButton(
                                onClick = {
                                    if (activity != null) {
                                        viewModel.onPhoneSubmit(viewModel.phoneNumber, activity)
                                        timeLeft = 60 // Bấm gửi lại thì reset thời gian
                                    }
                                },
                                enabled = timeLeft == 0 // Chỉ cho bấm khi thời gian đếm về 0
                            ) {
                                Text(
                                    text = if (timeLeft > 0) "Gửi lại sau ${timeLeft}s" else "Gửi lại ngay",
                                    fontWeight = FontWeight.Bold,
                                    color = if (timeLeft > 0) Color.Gray else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                LoginState.LOADING -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Đang xử lý...", color = Color.Gray)
                    }
                }

                LoginState.SUCCESS -> {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }

                LoginState.ERROR -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Thất bại",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mã OTP không đúng hoặc có lỗi xảy ra.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.resetToOtpState()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Thử lại OTP")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nút 2: Quay lại đổi số điện thoại
                        OutlinedButton(
                            onClick = {
                                viewModel.resetToPhoneState()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đổi số điện thoại khác")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                if (activity != null) viewModel.onPhoneSubmit(viewModel.phoneNumber, activity)
                            }
                        ) {
                            Text("Gửi lại mã mới")
                        }
                    }
                }
            }
        }
    }
}

// =================================================================
// COMPOSABLE TẠO 6 Ô VUÔNG NHẬP OTP
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
                horizontalArrangement = Arrangement.SpaceBetween,
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
                            .width(48.dp)
                            .height(56.dp)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = if (isFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    )
}
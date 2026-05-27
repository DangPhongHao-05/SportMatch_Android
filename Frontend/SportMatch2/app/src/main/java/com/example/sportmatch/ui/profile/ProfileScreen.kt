package com.example.sportmatch.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserId: Int,
    currentUserName: String,
    currentUserAvatar: String?,
    currentUserPhone: String,
    currentUserCreatedAt: String,
    onUpdateSystemData: (String, String?) -> Unit,
    onNavigateToBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    var editName by remember { mutableStateOf(currentUserName) }
    var editAvatarUrl by remember { mutableStateOf(currentUserAvatar ?: "") }

    // Biến lưu ảnh cục bộ vừa chọn từ máy
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    // Bộ mở Thư viện ảnh và gọi API Upload .NET
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            localImageUri = uri // Hiển thị tạm ảnh lên màn hình cho mượt

            // Gọi ViewModel bắn file lên Server C#
            profileViewModel.uploadAvatarToDotNetServer(
                context = context,
                imageUri = uri,
                userId = currentUserId,
                onSuccess = { publicUrl ->
                    editAvatarUrl = publicUrl // Cập nhật link Server trả về
                    Toast.makeText(context, "Tải ảnh lên Server thành công!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { errorMsg ->
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản cá nhân", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToBack, enabled = !profileViewModel.isSaving) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()) //Cho phép cuộn màn hình để không bị mất nút Đăng xuất
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- KHỐI 1: HIỂN THỊ HÌNH ẢNH AVATAR ---
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { //Cho phép bấm vào khối Avatar
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
            ) {
                // Ưu tiên hiển thị ảnh vừa chọn, nếu không thì hiển thị link trên mạng
                val imageToLoad = localImageUri ?: editAvatarUrl.takeIf { it.isNotBlank() }

                if (imageToLoad != null) {
                    AsyncImage(
                        model = imageToLoad,
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.size(110.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.padding(26.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- KHỐI 2: CARD TRẮNG CHỨA BIỂU MẪU THÔNG TIN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Thông tin định danh công khai", fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Họ tên hiển thị") },
                        singleLine = true,
                        enabled = !profileViewModel.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = currentUserPhone,
                        onValueChange = { },
                        label = { Text("Số điện thoại liên kết") },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.DarkGray,
                            disabledBorderColor = Color(0xFFE0E0E0),
                            disabledLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = currentUserCreatedAt,
                        onValueChange = { },
                        label = { Text("Ngày tham gia") },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.DarkGray,
                            disabledBorderColor = Color(0xFFE0E0E0),
                            disabledLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- KHỐI 3: NÚT ĐIỀU KHIỂN LƯU DỮ LIỆU CHÍNH ---
            Button(
                onClick = {
                    profileViewModel.updateUserProfile(
                        userId = currentUserId,
                        fullName = editName,
                        avatarUrl = editAvatarUrl,
                        onSuccess = { newName, newAvatar ->
                            onUpdateSystemData(newName, newAvatar)
                            Toast.makeText(context, "Đã lưu thay đổi tài khoản thành công!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { errorText ->
                            Toast.makeText(context, errorText, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !profileViewModel.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (profileViewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Lưu cấu hình", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- KHỐI 4: NÚT THOÁT TÀI KHOẢN (ĐĂNG XUẤT) ---
            OutlinedButton(
                onClick = {
                    profileViewModel.processLogout(userId = currentUserId) {
                        onLogoutSuccess()
                    }
                },
                enabled = !profileViewModel.isSaving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE91E63)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
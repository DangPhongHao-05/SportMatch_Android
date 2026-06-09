package com.example.sportmatch.ui.message

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportmatch.data.repository.ChatRepository
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.combinedClickable
import com.example.sportmatch.data.dto.MessageDto
import androidx.compose.foundation.ExperimentalFoundationApi

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
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MessageDto?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Launcher chọn ảnh
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadFileToFirebase(it, "image", currentUserId, targetUserId) }
    }

    // Launcher chọn file (PDF, DOC...)
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadFileToFirebase(it, "file", currentUserId, targetUserId) }
    }

    // BottomSheet hiển thị Emoji
    if (showEmojiPicker) {
        ModalBottomSheet(
            onDismissRequest = { showEmojiPicker = false },
            modifier = Modifier.fillMaxHeight(0.45f)
        ) {
            AndroidView(
                factory = { context ->
                    EmojiPickerView(context).apply {
                        setOnEmojiPickedListener { emoji ->
                            inputText += emoji.emoji
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }



    LaunchedEffect(Unit) {
        viewModel.startListening(currentUserId, targetUserId)
        val roomId = if (currentUserId < targetUserId) "${currentUserId}_${targetUserId}" else "${targetUserId}_${currentUserId}"
        ChatRepository().markAsRead(roomId)
    }

    // Cuộn xuống tin nhắn mới nhất (Bây giờ tin nhắn mới nhất nằm ở index 0)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .consumeWindowInsets(WindowInsets.navigationBars)
            .imePadding()
    ) {

        // 1. THANH TOP BAR
        TopAppBar(
            title = { Text(targetUserName, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
        )
        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

        // 2. DANH SÁCH TIN NHẮN
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true //  Đảo ngược layout, neo danh sách từ đáy lên
        ) {
            // Vì layout đã bị đảo ngược, nên vị trí khai báo item cũng bị đảo ngược:

            // Spacer này giờ nằm ở DƯỚI CÙNG (sát thanh nhập chat)
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Đảo ngược mảng tin nhắn để hiển thị đúng thứ tự từ trên xuống dưới
            items(messages.asReversed()) { msg ->
                val isMeMessage = msg.senderId == currentUserId
                MessageBubble(
                    text = msg.text,
                    isMe = msg.senderId == currentUserId,
                    fileUrl = msg.fileUrl, // fileUrl từ message vào
                    kind = msg.kind,       // type (image/file) vào
                    onLongClick = {
                        if (isMeMessage) { // Chỉ cho phép thao tác với tin nhắn của chính mình
                            selectedMessage = msg
                            showActionDialog = true
                        }
                    }

                )
            }

            // Spacer này giờ nằm ở TRÊN CÙNG (sát TopAppBar)
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // 3. THANH NHẬP TIN NHẮN
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
                // Nút Ảnh
                IconButton(onClick = { imagePicker.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = "Ảnh")
                }

                // Nút File
                IconButton(onClick = { filePicker.launch("*/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "File")
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhập tin nhắn...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            keyboardController?.hide() // Ẩn bàn phím trước khi hiện Emoji
                            showEmojiPicker = !showEmojiPicker
                        }) {
                            Icon(
                                imageVector = Icons.Default.Mood,
                                contentDescription = "Emoji"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(currentUserId, targetUserId, inputText)
                            inputText = ""
                            keyboardController?.hide() // Tự động đóng bàn phím sau khi gửi
                        }
                    },
                    modifier = Modifier.size(44.dp).background(Color(0xFF2196F3), CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // DIALOG CHỌN TÙY CHỌN (XÓA / SỬA)
    if (showActionDialog && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Tùy chọn", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn muốn làm gì với tin nhắn này?") },
            confirmButton = {
                TextButton(onClick = {
                    showActionDialog = false
                    viewModel.deleteMessage(currentUserId, targetUserId, selectedMessage!!.messageId)
                }) { Text("Xóa", color = Color.Red) }
            },
            dismissButton = {
                // Chỉ cho phép sửa nếu đó là tin nhắn chữ
                if (selectedMessage!!.kind == "text") {
                    TextButton(onClick = {
                        showActionDialog = false
                        editText = selectedMessage!!.text // Gán text cũ vào ô nhập
                        showEditDialog = true
                    }) { Text("Sửa", color = Color.Blue) }
                }
            }
        )
    }

    // DIALOG NHẬP NỘI DUNG SỬA TIN NHẮN
    if (showEditDialog && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Sửa tin nhắn", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editMessage(currentUserId, targetUserId, selectedMessage!!.messageId, editText)
                    showEditDialog = false
                }) { Text("Lưu", color = Color.Blue) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Hủy", color = Color.Gray) }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    text: String,
    fileUrl: String?,
    kind: String?,
    isMe: Boolean,
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onClick = { /* Bấm bình thường không làm gì cả */ },
                    onLongClick = { onLongClick() } // Gọi hàm khi nhấn giữ
                )
        ) {
            // Tách biệt cách vẽ dựa trên loại tin nhắn
            if (kind == "picture" || kind == "image") {
                AsyncImage(
                    model = fileUrl,
                    contentDescription = "Ảnh",
                    modifier = Modifier
                        .size(200.dp) // Cố định kích thước ảnh
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                // Xác định màu chữ và màu icon dựa trên người gửi
                val contentColor = if (isMe) Color.White else Color.Black

                Column(
                    modifier = Modifier
                        // Áp dụng background và bo góc của bong bóng chat ở đây
                        .background(
                            color = if (isMe) Color(0xFF2196F3) else Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        // Phần đệm bên trong bong bóng chat
                        .padding(8.dp)
                ) {
                    when (kind) {
                        "file" -> {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                                        context.startActivity(intent)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sử dụng màu động contentColor để không bị trùng màu nền
                                Icon(Icons.Default.AttachFile, contentDescription = "Tệp", tint = contentColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tệp đính kèm", color = contentColor)
                            }
                        }
                        else -> { // Tin nhắn Chữ (text)
                            Text(
                                text = text,
                                color = contentColor, // Sử dụng màu động contentColor
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
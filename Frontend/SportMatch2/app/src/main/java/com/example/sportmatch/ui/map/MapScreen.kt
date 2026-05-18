package com.example.sportmatch.ui.match

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.sportmatch.data.dto.CreateMatchDto
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.setValue
import com.example.sportmatch.data.model.NearbyMatchResponse

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onNavigateToBack: () -> Unit,
    onNavigateToChat: (receiverId: Int) -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    // 1. Quản lý trạng thái xin quyền Vị trí
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    var isCameraInitialized by remember { mutableStateOf(false) }
    var currentLatLng by remember { mutableStateOf(LatLng(13.7592, 109.2190)) } // Vị trí GPS gốc của máy

    // Tọa độ click chọn tự do hoặc tìm kiếm (Xanh dương)
    var selectedCustomLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 14f)
    }

    // Trạng thái Giao diện nâng cao
    var searchQuery by remember { mutableStateOf("") }
    var selectedMatchDetail by remember { mutableStateOf<NearbyMatchResponse?>(null) }

    // Trạng thái Form Đăng kèo BottomSheet
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sportType by remember { mutableStateOf("") }
    var missingPlayers by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // TỰ ĐỘNG KHỞI TẠO & FORMAT NGÀY GIỜ HIỆN TẠI CHUẨN XÁC
    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(sdfDate.format(Date())) }
    var selectedTime by remember { mutableStateOf(sdfTime.format(Date())) }

    if (locationPermissionState.status.isGranted) {

        // LUỒNG 1: Khởi tạo lấy vị trí GPS thực tế khi mở màn hình lần đầu
        LaunchedEffect(Unit) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    currentLatLng = userLocation
                    if (selectedCustomLatLng == null) {
                        selectedCustomLatLng = userLocation
                    }
                    if (!isCameraInitialized) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 14f)
                        isCameraInitialized = true
                    }
                } else {
                    val defaultLoc = LatLng(13.7592, 109.2190)
                    currentLatLng = defaultLoc
                    if (selectedCustomLatLng == null) {
                        selectedCustomLatLng = defaultLoc
                    }
                    if (!isCameraInitialized) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultLoc, 14f)
                        isCameraInitialized = true
                    }
                }
            }
        }

        // LUỒNG 2: Gọi API cập nhật danh sách ghim dựa vào điểm tự chọn quét được
        LaunchedEffect(selectedCustomLatLng) {
            selectedCustomLatLng?.let { targetLoc ->
                viewModel.fetchNearbyMatches(
                    lat = targetLoc.latitude,
                    lng = targetLoc.longitude,
                    radiusInKm = 10.0
                )
            }
        }

        // --- BỐ CỤC GIAO DIỆN CHÍNH ---
        Box(modifier = Modifier.fillMaxSize()) {

            // LỚP 1: BẢN ĐỒ GOOGLE MAPS
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false, // Tắt mặc định để tự làm cụm nút bên sườn phải
                    zoomControlsEnabled = false      // Tắt mặc định để tự vẽ nút thu phóng chuẩn giao diện
                ),
                onMapClick = { latLng ->
                    // Hành vi: Click bất kỳ đâu trên bản đồ để dời ghim xanh dương chọn vị trí mới
                    selectedCustomLatLng = latLng
                    selectedMatchDetail = null // Đóng bảng chi tiết đường đi nếu đang mở
                }
            ) {
                // Ghim màu XANH DƯƠNG: Điểm do bạn tự chọn (Click hoặc tìm kiếm)
                selectedCustomLatLng?.let { customLoc ->
                    Marker(
                        state = MarkerState(position = customLoc),
                        title = "Vị trí bạn chọn",
                        snippet = "Đăng tìm trận hoặc dẫn đường xuất phát từ đây",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }

                // Ghim màu ĐỎ: Danh sách các kèo đấu quét từ Backend C# về quanh vị trí chọn
                viewModel.nearbyMatches.forEach { match ->
                    if (match.latitude in -90.0..90.0 && match.longitude in -180.0..180.0) {
                        Marker(
                            state = MarkerState(position = LatLng(match.latitude, match.longitude)),
                            title = "${match.sportType} - Thiếu ${match.missingPlayers} người",
                            snippet = "Chủ sân: ${match.hostName} (Cách vị trí chọn ${match.distanceInMeters.toInt()}m)",
                            onClick = {
                                // Gán dữ liệu trận đấu được chọn để kích hoạt thanh tìm đường đi bên dưới
                                selectedMatchDetail = match
                                true
                            }
                        )
                    }
                }
            }

            // LỚP 2: THANH SEARCH ĐỊA ĐIỂM + NÚT QUAY LẠI (TOP BAR)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔙 NÚT QUAY LẠI MÀN HÌNH TRƯỚC
                IconButton(
                    onClick = { onNavigateToBack() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White),
                    modifier = Modifier
                        .size(44.dp)
                        .padding(end = 4.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }

                // THANH TÌM KIẾM ĐỊA DANH HÌNH VIÊN THUỐC ĐẸP MẮT
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Tìm địa điểm, sân bóng...", color = Color.Gray, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                // Thực thi tìm kiếm tọa độ thông qua văn bản (Geocoder)
                                try {
                                    val addresses = geocoder.getFromLocationName(searchQuery, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val address = addresses[0]
                                        val searchedLatLng = LatLng(address.latitude, address.longitude)

                                        // Chuyển vị trí chọn và dời máy ảnh về điểm vừa tìm kiếm
                                        selectedCustomLatLng = searchedLatLng
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(searchedLatLng, 15f)
                                        searchQuery = "" // Clear thanh chữ sau khi tìm thấy
                                    } else {
                                        Toast.makeText(context, "Không tìm thấy địa điểm này!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: IOException) {
                                    Toast.makeText(context, "Lỗi kết nối định vị tìm kiếm!", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Go", tint = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            }

            // LỚP 3: CỤM NÚT ĐIỀU KHIỂN SƯỜN PHẢI
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nút Phóng to bản đồ
                FloatingActionButton(
                    onClick = {
                        val currentZoom = cameraPositionState.position.zoom
                        cameraPositionState.move(CameraUpdateFactory.zoomTo(currentZoom + 1f))
                    },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                // Nút Thu nhỏ bản đồ (Đổi từ Clear sang Remove chuẩn chỉ)
                FloatingActionButton(
                    onClick = {
                        val currentZoom = cameraPositionState.position.zoom
                        cameraPositionState.move(CameraUpdateFactory.zoomTo(currentZoom - 1f))
                    },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                // Nút hồi tâm GPS (Về lại vị trí chấm xanh định vị thực tế)
                FloatingActionButton(
                    onClick = {
                        selectedCustomLatLng = currentLatLng
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF2196F3),
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }

            // LỚP 4: THÀNH PHẦN THÔNG TIN CHI TIẾT & CHỈ ĐƯỜNG (CÓ CHAT & GỌI ĐIỆN)
            AnimatedVisibility(
                visible = selectedMatchDetail != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                selectedMatchDetail?.let { matchData ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // --- PHẦN 1: HEADER (AVATAR, TÊN, KHOẢNG CÁCH) ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = Color(0xFFE3F2FD),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = matchData.hostName,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = "Distance", tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Cách bạn ${matchData.distanceInMeters.toInt()}m",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedMatchDetail = null },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFF5F5F5), androidx.compose.foundation.shape.CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.DarkGray, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // --- PHẦN 2: THÔNG TIN CHI TIẾT TRẬN ĐẤU ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Sport", tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "Môn thể thao", fontSize = 12.sp, color = Color.Gray)
                                        Text(text = matchData.sportType, fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = "Missing", tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "Đang thiếu", fontSize = 12.sp, color = Color.Gray)
                                        Text(text = "${matchData.missingPlayers} người", fontSize = 15.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Schedule, contentDescription = "Time", tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Thời gian rảnh", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = matchData.endTime.replace("T", "  -  "),
                                        fontSize = 15.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = "Note", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Ghi chú", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = if (matchData.description.isNullOrBlank()) "không có ghi chú" else matchData.description,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // --- PHẦN 3: CỤM NÚT CHỨC NĂNG PHỤ (GỌI ĐIỆN & LIÊN HỆ CHAT) ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // NÚT GỌI ĐIỆN TRỰC TIẾP
                                OutlinedButton(
                                    onClick = {
                                        val phoneNumber = matchData.hostPhone
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phoneNumber")
                                        }
                                        context.startActivity(dialIntent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gọi điện", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // NÚT LIÊN HỆ CHAT
                                OutlinedButton(
                                    onClick = {
                                        onNavigateToChat(matchData.hostId)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2196F3)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3))
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Chat")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Nhắn tin", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // --- PHẦN 4: NÚT XEM ĐƯỜNG ĐI CHÍNH ---
                            Button(
                                onClick = {
                                    val startLoc = selectedCustomLatLng ?: currentLatLng
                                    // gọi qua bản đồ bên gg map
                                    val uriStr = "http://maps.google.com/maps?saddr=$\${startLoc.latitude},${startLoc.longitude}&daddr=${matchData.latitude},${matchData.longitude}"

                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Thiết bị chưa cài Google Maps!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                            ) {
                                Icon(Icons.Default.Place, contentDescription = "Route", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Xem đường đi đến sân", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // NÚT CHÍNH ĐỂ MỞ FORM ĐĂNG TÌM ĐỘI (Góc dưới bên phải)
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text(text = "Tìm đội") },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 32.dp)
            )

            // FORM BOTTOM SHEET ĐĂNG TÌM TEAM (Lấy vị trí tự chọn cắm ghim xanh làm tọa độ lưu DB)
            if (showBottomSheet) {
                val calendar = Calendar.getInstance()

                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = System.currentTimeMillis()
                }

                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )

                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            // Sử dụng imePadding để tự động đẩy cả form lên khi bàn phím xuất hiện
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = "Tạo yêu cầu", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = sportType,
                            onValueChange = { sportType = it },
                            label = { Text("Môn thể thao") },
                            placeholder = { Text("VD: Bóng chuyền, Cầu lông...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Ngày rảnh") },
                            trailingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Chọn ngày", tint = Color(0xFF4CAF50))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Giờ rảnh") },
                            trailingIcon = {
                                IconButton(onClick = { timePickerDialog.show() }) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = "Chọn giờ", tint = Color(0xFF2196F3))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = missingPlayers,
                            onValueChange = { missingPlayers = it },
                            label = { Text("Số người thiếu") },
                            placeholder = { Text("VD: 1") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Ghi chú") },
                            placeholder = { Text("VD: Sân trường Đại học Quy Nhơn...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Nút xác nhận chính
                        Button(
                            onClick = {
                                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                val formattedStartTime = sdf.format(Date())

                                val finalDate = if (selectedDate.isNotBlank()) selectedDate else sdfDate.format(Date())
                                val finalTime = if (selectedTime.isNotBlank()) selectedTime else sdfTime.format(Date())
                                val formattedEndTime = "${finalDate}T${finalTime}:00"

                                val targetLoc = selectedCustomLatLng ?: currentLatLng

                                val newMatchDto = CreateMatchDto(
                                    hostId = 1,
                                    sportType = sportType,
                                    requestType = "FindPlayer",
                                    missingPlayers = missingPlayers.toIntOrNull() ?: 1,
                                    description = description,
                                    startTime = formattedStartTime,
                                    endTime = formattedEndTime,
                                    latitude = targetLoc.latitude,
                                    longitude = targetLoc.longitude
                                )

                                viewModel.createNewMatch(newMatchDto) {
                                    showBottomSheet = false
                                    sportType = ""
                                    selectedDate = sdfDate.format(Date())
                                    selectedTime = sdfTime.format(Date())
                                    missingPlayers = ""
                                    description = ""

                                    viewModel.fetchNearbyMatches(
                                        lat = targetLoc.latitude,
                                        lng = targetLoc.longitude,
                                        radiusInKm = 10.0
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(text = "Xác nhận", color = Color.White)
                        }
                    }
                }
            }

        }
    } else {
        // --- GIAO DIỆN XIN QUYỀN VỊ TRÍ ---
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ứng dụng cần quyền định vị để quét các trận đấu thể thao xung quanh bạn.",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { locationPermissionState.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(text = "Cấp quyền vị trí", color = Color.White)
            }
        }
    }
}
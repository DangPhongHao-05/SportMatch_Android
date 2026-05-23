package com.example.sportmatch.ui.profile

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportmatch.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()

    // Quản lý trạng thái giao diện Loading nội bộ của trang Profile
    var isSaving by mutableStateOf(false)
        private set

    // Hàm gọi API cập nhật thông tin lên Backend .NET
    fun updateUserProfile(
        userId: Int,
        fullName: String,
        avatarUrl: String?,
        onSuccess: (String, String) -> Unit, // Trả về Tên mới và Avatar mới để cập nhật RAM tổng
        onFailure: (String) -> Unit
    ) {
        if (fullName.isBlank()) {
            onFailure("Họ và tên không được để trống!")
            return
        }

        viewModelScope.launch {
            isSaving = true
            try {
                val response = withContext(Dispatchers.IO) {
                    profileRepository.updateProfile(userId, fullName, avatarUrl?.ifBlank { null })
                }

                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!.user
                    Log.d("PROFILE_DEBUG", "Cập nhật MySQL thành công: ${updatedUser.fullName}")

                    // Callback báo cho giao diện biết để đồng bộ lại RAM hệ thống
                    onSuccess(updatedUser.fullName, updatedUser.avatarUrl ?: "")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi phản hồi từ Server"
                    Log.e("PROFILE_DEBUG", "Server từ chối: $errorMsg")
                    onFailure("Cập nhật thất bại từ Server!")
                }
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Lỗi kết nối mạng: ${e.message}")
                onFailure("Lỗi kết nối mạng, vui lòng thử lại!")
            } finally {
                isSaving = false
            }
        }
    }

    // Hàm xử lý đăng xuất an toàn
    fun processLogout(userId: Int, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            isSaving = true

            try {
                // 1. Gọi Repository để ném request sang Server .NET
                val response = withContext(Dispatchers.IO) {
                    profileRepository.logoutBackend(userId)
                }

                // 2. ViewModel tự đánh giá kết quả từ Server
                if (response.isSuccessful) {
                    Log.d("LOGOUT_FLOW", "Đã xóa phiên/FCM Token thành công trên Server .NET")
                } else {
                    Log.e("LOGOUT_FLOW", "Server .NET từ chối: ${response.code()} - ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                // Hứng lỗi rớt mạng hoặc sập Server
                Log.e("LOGOUT_FLOW", "Lỗi kết nối mạng khi Logout: ${e.message}")

            } finally {
                // 3. KHÚC QUAN TRỌNG NHẤT: Bất chấp Server có phản hồi hay rớt mạng (chạy vào catch),
                // thì cuối cùng (finally) vẫn BẮT BUỘC phải dọn Firebase cục bộ và đẩy người dùng ra ngoài!

                FirebaseAuth.getInstance().signOut()
                Log.d("LOGOUT_FLOW", "Đã dọn sạch Firebase Auth cục bộ.")

                isSaving = false
                onLogoutComplete() // Kích hoạt văng về màn hình Login
            }
        }
    }

    // Hàm chuyển đổi ảnh từ thiết bị và bắn lên .NET Server
    fun uploadAvatarToDotNetServer(
        context: Context,
        imageUri: Uri,
        userId: Int,
        onSuccess: (String) -> Unit, // Nhận về link URL từ .NET cấp
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSaving = true
            try {
                // 1. Đọc luồng dữ liệu (InputStream) từ Uri hình ảnh trong máy điện thoại
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    onFailure("Không thể đọc dữ liệu hình ảnh!")
                    return@launch
                }

                // 2. Đóng gói mảng Bytes thành RequestBody của OkHttp
                val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size)

                // 3. Tạo MultipartBody.Part (Khớp với cái tham số "file" bên hàm C#)
                val filePart = MultipartBody.Part.createFormData("file", "avatar_upload.jpg", requestFile)

                // 4. Tạo RequestBody cho tham số userId (Khớp với "userId" bên C#)
                val userIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // 5. Phóng API lên Server .NET
                val response = withContext(Dispatchers.IO) {
                    profileRepository.uploadAvatar(filePart, userIdPart)
                }

                // 6. Xử lý phản hồi trả về
                if (response.isSuccessful && response.body() != null) {
                    val serverUrl = response.body()!!.avatarUrl
                    Log.d("UPLOAD_NET", "Server cấp link ảnh thành công: $serverUrl")
                    onSuccess(serverUrl)
                } else {
                    Log.e("UPLOAD_NET", "Server từ chối file: ${response.errorBody()?.string()}")
                    onFailure("Server từ chối tiếp nhận file ảnh!")
                }

            } catch (e: Exception) {
                Log.e("UPLOAD_NET", "Lỗi luồng mạng: ${e.message}")
                onFailure("Lỗi kết nối mạng đến Server .NET!")
            } finally {
                isSaving = false
            }
        }
    }
}
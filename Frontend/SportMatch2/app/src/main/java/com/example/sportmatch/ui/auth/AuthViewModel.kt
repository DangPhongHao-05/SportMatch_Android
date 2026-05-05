package com.example.sportmatch.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportmatch.data.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

enum class LoginState {
    PHONE_INPUT,
    OTP_INPUT,
    LOADING,
    SUCCESS,
    ERROR
}

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState.PHONE_INPUT)
    private val authRepository = AuthRepository()
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    // Thêm các biến để hứng thông tin User
    var userId: Int = -1
    var userFullName: String = ""
    var userAvatar: String? = null
    var phoneNumber = ""
    private var storedVerificationId = "" // Giữ lại ID này để khớp với OTP người dùng nhập

    // 1. Hàm gọi khi bấm "Gửi OTP"
    fun onPhoneSubmit(phone: String, activity: Activity) {
        _uiState.value = LoginState.LOADING

        // BƯỚC 1: LÀM SẠCH CHUỖI NHẬP VÀO
        // Xóa toàn bộ khoảng trắng, dấu gạch ngang hoặc ký tự lạ, chỉ giữ lại số và dấu +
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")

        // BƯỚC 2: CHUẨN HÓA VỀ ĐỊNH DẠNG +84
        val formatPhone = when {
            cleanPhone.startsWith("+84") -> cleanPhone
            cleanPhone.startsWith("84") -> "+$cleanPhone"
            cleanPhone.startsWith("0") -> "+84${cleanPhone.substring(1)}"
            else -> "+84$cleanPhone"
        }

        // Lưu lại số đã chuẩn hóa để hiển thị lên giao diện UI cho đẹp
        phoneNumber = formatPhone

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formatPhone)       // Số điện thoại cần gửi
            .setTimeout(60L, TimeUnit.SECONDS) // Thời gian chờ
            .setActivity(activity)             // Activity hiện tại
            .setCallbacks(callbacks)           // Các hàm lắng nghe kết quả
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 2. Bộ lắng nghe kết quả từ Firebase khi yêu cầu gửi SMS
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        // Firebase tự động xác thực thành công (không cần nhập OTP)
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        // Bị lỗi (Sai số, chưa cấu hình SHA-1/SHA-256, bị block...)
        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("Firebase", "Gửi SMS thất bại: ${e.message}")
            _uiState.value = LoginState.ERROR
        }

        // SMS đã được gửi đi thành công, chuyển sang màn nhập OTP
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            storedVerificationId = verificationId
            _uiState.value = LoginState.OTP_INPUT
        }
    }

    // 3. Hàm gọi khi người dùng gõ xong 6 số và bấm "Xác nhận"
    fun onOtpSubmit(otpCode: String) {
        _uiState.value = LoginState.LOADING
        // Đóng gói verificationId và mã OTP thành một Credential
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpCode)
        signInWithPhoneAuthCredential(credential)
    }

    // 4. Hàm thực hiện đăng nhập và lấy IdToken
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Đăng nhập Firebase thành công, tiến hành lấy IdToken
                    val user = task.result?.user
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            // Sử dụng toán tử Elvis ?: để lấy chuỗi rỗng nếu null,
                            // hoặc dùng if (idToken != null)
                            val idToken = tokenTask.result?.token
                            Log.d("TOKEN_TEST", "Chuoi Token: $idToken")

                            if (idToken != null) {
                                verifyBackend(idToken) // Lúc này idToken chắc chắn là String, không còn là String?
                            } else {
                                Log.e("Auth", "Firebase Token bị null")
                                _uiState.value = LoginState.ERROR
                            }
                        } else {
                            _uiState.value = LoginState.ERROR
                        }
                    }
                } else {
                    Log.e("Firebase", "Sai mã OTP")
                    _uiState.value = LoginState.ERROR
                }
            }
    }

    private fun verifyBackend(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginState.LOADING
            try {
                val response = withContext(Dispatchers.IO) {
                    authRepository.verifyTokenWithBackend(idToken)
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val systemToken = data.token
                    val user = data.user

                    Log.d("AUTH_SUCCESS", "Chào mừng ${user.fullName}, ID của bạn là ${user.id}")

                    // Lưu token và thông tin cần thiết rồi chuyển màn
                    delay(500)
                    _uiState.value = LoginState.SUCCESS
                } else {
                    _uiState.value = LoginState.ERROR
                }
            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Lỗi kết nối: ${e.message}")
                _uiState.value = LoginState.ERROR
            }
        }
    }

    // Hàm này giúp đưa giao diện quay lại màn hình nhập OTP nếu bị lỗi
    fun resetToOtpState() {
        _uiState.value = LoginState.OTP_INPUT
    }

    // Hàm này giúp đưa giao diện quay lại từ đầu để nhập số khác
    fun resetToPhoneState() {
        _uiState.value = LoginState.PHONE_INPUT
    }
}
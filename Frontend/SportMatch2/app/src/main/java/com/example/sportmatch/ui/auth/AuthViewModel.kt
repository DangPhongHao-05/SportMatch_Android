package com.example.sportmatch.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var userId by mutableStateOf(-1)
        private set
    var userFullName by mutableStateOf("Người dùng")
        private set
    var userAvatar by mutableStateOf<String?>(null)
        private set
    var phoneNumber by mutableStateOf("")
        private set

    private var storedVerificationId = ""

    // 1. Hàm gọi khi bấm "Gửi OTP"
    fun onPhoneSubmit(phone: String, activity: Activity) {
        _uiState.value = LoginState.LOADING

        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")

        val formatPhone = when {
            cleanPhone.startsWith("+84") -> cleanPhone
            cleanPhone.startsWith("84") -> "+$cleanPhone"
            cleanPhone.startsWith("0") -> "+84${cleanPhone.substring(1)}"
            else -> "+84$cleanPhone"
        }

        phoneNumber = formatPhone

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formatPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 2. Bộ lắng nghe kết quả từ Firebase khi yêu cầu gửi SMS
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("Firebase", "Gửi SMS thất bại: ${e.message}")
            _uiState.value = LoginState.ERROR
        }

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
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpCode)
        signInWithPhoneAuthCredential(credential)
    }

    // 4. Hàm thực hiện đăng nhập và lấy IdToken
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token
                            Log.d("TOKEN_TEST", "Chuoi Token: $idToken")

                            if (idToken != null) {
                                verifyBackend(idToken)
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

                    userId = user.id
                    userFullName = user.fullName

                    Log.d("AUTH_SUCCESS", "Chào mừng $userFullName, ID của bạn là $userId")

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

    fun resetToOtpState() {
        _uiState.value = LoginState.OTP_INPUT
    }

    fun resetToPhoneState() {
        _uiState.value = LoginState.PHONE_INPUT
    }
}
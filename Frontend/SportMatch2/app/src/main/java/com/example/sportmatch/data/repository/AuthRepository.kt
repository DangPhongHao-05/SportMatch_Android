package com.example.sportmatch.data.repository

import android.util.Log
import com.example.sportmatch.data.api.RetrofitClient
import com.example.sportmatch.data.dto.AuthResponseDto
import com.example.sportmatch.data.dto.FcmTokenUpdateDto
import com.example.sportmatch.data.dto.VerifyFirebaseRequestDto
import retrofit2.Response

class AuthRepository {
    suspend fun verifyTokenWithBackend(idToken: String): Response<AuthResponseDto> {
        val request = VerifyFirebaseRequestDto(idToken = idToken)
        return RetrofitClient.authApi.verifyPhoneAuth(request)
    }

    suspend fun updateFcmToken(userId: Int, token: String): Boolean {
        return try {
            val response = RetrofitClient.authApi.updateFcmToken(FcmTokenUpdateDto(userId, token))

            if (!response.isSuccessful) {
                // IN LỖI RA LOG ĐỂ XEM SERVER NÓ CHỬI CÁI GÌ
                Log.e("FCM_FLOW", "Server trả về lỗi: ${response.code()} - ${response.errorBody()?.string()}")
            }

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("FCM_FLOW", "Lỗi kết nối: ${e.message}")
            false
        }
    }
}
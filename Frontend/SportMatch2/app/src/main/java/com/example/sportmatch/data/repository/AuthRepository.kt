package com.example.sportmatch.data.repository

import com.example.sportmatch.data.api.RetrofitClient
import com.example.sportmatch.data.dto.AuthResponse
import com.example.sportmatch.data.dto.VerifyFirebaseRequest
import retrofit2.Response

class AuthRepository {
    suspend fun verifyTokenWithBackend(idToken: String): Response<AuthResponse> {
        val request = VerifyFirebaseRequest(idToken = idToken)
        return RetrofitClient.authApi.verifyPhoneAuth(request)
    }
}
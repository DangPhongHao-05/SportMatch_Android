package com.example.sportmatch.data.api

import com.example.sportmatch.data.model.VerifyFirebaseRequest
import com.example.sportmatch.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/Auth/verify-phone")
    suspend fun verifyPhoneAuth(@Body request: VerifyFirebaseRequest): Response<AuthResponse>
}
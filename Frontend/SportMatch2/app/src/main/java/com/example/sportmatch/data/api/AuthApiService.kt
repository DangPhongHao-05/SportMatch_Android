package com.example.sportmatch.data.api

import com.example.sportmatch.data.dto.AuthResponseDto
import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.FcmTokenUpdateDto
import com.example.sportmatch.data.dto.VerifyFirebaseRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/Auth/verify-phone")
    suspend fun verifyPhoneAuth(@Body request: VerifyFirebaseRequestDto): Response<AuthResponseDto>

    @POST("api/auth/update-token")
    suspend fun updateFcmToken(@Body request: FcmTokenUpdateDto): Response<BaseResponseDto>
}
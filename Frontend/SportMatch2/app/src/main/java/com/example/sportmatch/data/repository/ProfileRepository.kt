package com.example.sportmatch.data.repository

import com.example.sportmatch.data.api.RetrofitClient
import com.example.sportmatch.data.dto.AuthResponseDto
import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.LogoutRequestDto
import com.example.sportmatch.data.dto.UpdateProfileDto
import com.example.sportmatch.data.dto.UploadAvatarDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class ProfileRepository {
    suspend fun updateProfile(id: Int, fullName: String, avatarUrl: String?): Response<AuthResponseDto> {
        val dto = UpdateProfileDto(id = id, fullName = fullName, avatarUrl = avatarUrl)
        return RetrofitClient.userApi.updateProfile(dto)
    }

    suspend fun logoutBackend(userId: Int): Response<BaseResponseDto> {
        val request = LogoutRequestDto(userId = userId)
        return RetrofitClient.userApi.logoutResponse(request)
    }

    suspend fun uploadAvatar(file: MultipartBody.Part, userId: RequestBody): Response<UploadAvatarDto> {
        return RetrofitClient.userApi.uploadAvatar(file, userId)
    }
}
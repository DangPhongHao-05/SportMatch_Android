package com.example.sportmatch.data.api

import com.example.sportmatch.data.dto.AuthResponseDto
import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.LogoutRequestDto
import com.example.sportmatch.data.dto.UpdateProfileDto
import com.example.sportmatch.data.dto.UploadAvatarDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserApiService {
    @POST("api/user/update-profile")
    suspend fun updateProfile(@Body request: UpdateProfileDto): Response<AuthResponseDto>

    @POST("api/user/logout")
    suspend fun logoutResponse(@Body request: LogoutRequestDto): Response<BaseResponseDto>

    @Multipart
    @POST("api/user/upload-avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part,       // Chứa file ảnh nhị phân
        @Part("userId") userId: RequestBody   // Chứa ID người dùng dạng text form
    ): Response<UploadAvatarDto>
}
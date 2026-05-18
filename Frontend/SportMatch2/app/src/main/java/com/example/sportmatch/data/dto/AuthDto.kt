package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

// 1. DTO gửi lên Backend để xác thực Token Firebase
data class VerifyFirebaseRequest(
    @SerializedName("idToken") val idToken: String
)

// 2. DTO hứng toàn bộ thông tin User từ Backend trả về
data class AuthResponse(
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

// 3. DTO chi tiết User đi kèm trong AuthResponse
data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?
)
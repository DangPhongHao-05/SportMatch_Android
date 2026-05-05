package com.example.sportmatch.data.model

import com.google.gson.annotations.SerializedName

data class VerifyFirebaseRequest(
    @SerializedName("idToken") val idToken: String
)

// Model hứng toàn bộ thông tin User từ Backend trả về
data class AuthResponse(
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?
)
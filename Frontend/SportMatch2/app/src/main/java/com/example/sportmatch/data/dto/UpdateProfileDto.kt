package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class UpdateProfileDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?
)
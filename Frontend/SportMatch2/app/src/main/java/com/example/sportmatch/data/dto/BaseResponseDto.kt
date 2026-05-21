package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class BaseResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class FcmTokenUpdateDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("token") val token: String
)
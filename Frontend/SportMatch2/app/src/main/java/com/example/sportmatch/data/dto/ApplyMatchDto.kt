package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class ApplyMatchDto(
    @SerializedName("matchRequestId")
    val matchRequestId: Int,
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("message")
    val message: String
)
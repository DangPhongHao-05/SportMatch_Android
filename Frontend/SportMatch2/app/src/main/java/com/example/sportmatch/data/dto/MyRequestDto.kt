package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class MyRequestDto(
    @SerializedName("id") val id: Int,
    @SerializedName("matchRequestId") val matchRequestId: Int,
    @SerializedName("hostName") val hostName: String,
    @SerializedName("sportType") val sportType: String,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String
)
package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class CreateMatchDto(
    @SerializedName("hostId") val hostId: Int,
    @SerializedName("sportType") val sportType: String,
    @SerializedName("requestType") val requestType: String,
    @SerializedName("missingPlayers") val missingPlayers: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
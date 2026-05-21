package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class NearbyMatchResponseDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("hostId")
    val hostId: Int,
    @SerializedName("hostName")
    val hostName: String,
    @SerializedName("hostPhone")
    val hostPhone: String?,
    @SerializedName("sportType")
    val sportType: String,
    @SerializedName("requestType")
    val requestType: String,
    @SerializedName("missingPlayers")
    val missingPlayers: Int,
    @SerializedName("description")
    val description: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("distanceInMeters")
    val distanceInMeters: Double
)
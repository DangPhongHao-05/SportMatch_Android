package com.example.sportmatch.data.dto

data class NearbyMatchResponseDto(
    val id: Int,
    val hostId: Int,
    val hostName: String,
    val hostPhone: String?,
    val sportType: String,
    val requestType: String,
    val missingPlayers: Int,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val latitude: Double,
    val longitude: Double,
    val distanceInMeters: Double
)
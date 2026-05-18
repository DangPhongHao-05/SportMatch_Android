package com.example.sportmatch.data.model

// Model dùng để hứng dữ liệu danh sách trận đấu thật từ Backend .NET trả về
data class NearbyMatchResponse(
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
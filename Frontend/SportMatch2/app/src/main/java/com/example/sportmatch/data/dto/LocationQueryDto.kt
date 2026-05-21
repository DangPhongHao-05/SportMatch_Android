package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class LocationQueryDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("radiusInKm") val radiusInKm: Double = 5.0,
    @SerializedName("sportType") val sportType: String? = null,
    @SerializedName("filterDate") val filterDate: String? = null
)
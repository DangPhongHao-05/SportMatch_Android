package com.example.sportmatch.data.repository

import com.example.sportmatch.data.model.NearbyMatchResponse
import com.example.sportmatch.data.api.RetrofitClient
import com.example.sportmatch.data.dto.CreateMatchDto
import com.example.sportmatch.data.dto.LocationQueryDto
import retrofit2.Response

class MapRepository {
    private val apiService = RetrofitClient.mapApi

    suspend fun getNearbyMatches(lat: Double, lng: Double, radius: Double): Response<List<NearbyMatchResponse>> {
        return apiService.getNearbyMatches(LocationQueryDto(lat, lng, radius))
    }
    suspend fun createMatch(matchData: CreateMatchDto): Response<Unit> {
        return apiService.createMatch(matchData)
    }
}
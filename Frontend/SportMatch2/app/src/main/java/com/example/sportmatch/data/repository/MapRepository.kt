package com.example.sportmatch.data.repository

import com.example.sportmatch.data.api.RetrofitClient
import com.example.sportmatch.data.dto.CreateMatchDto
import com.example.sportmatch.data.dto.LocationQueryDto
import com.example.sportmatch.data.dto.ApplyMatchDto
import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.NearbyMatchResponseDto
import retrofit2.Response

class MapRepository {
    private val apiService = RetrofitClient.mapApi

    suspend fun getNearbyMatches(lat: Double, lng: Double, radius: Double, sportType: String?, filterDate: String?): Response<List<NearbyMatchResponseDto>> {
        return apiService.getNearbyMatches(LocationQueryDto(lat, lng, radius, sportType, filterDate))
    }

    suspend fun createMatch(matchData: CreateMatchDto): Response<Unit> {
        return apiService.createMatch(matchData)
    }

    suspend fun applyForMatch(dto: ApplyMatchDto): Response<BaseResponseDto> {
        return apiService.applyForMatch(dto)
    }
}
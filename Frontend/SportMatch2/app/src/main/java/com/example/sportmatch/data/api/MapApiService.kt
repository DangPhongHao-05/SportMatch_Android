package com.example.sportmatch.data.api

import com.example.sportmatch.data.dto.CreateMatchDto
import com.example.sportmatch.data.dto.LocationQueryDto
import com.example.sportmatch.data.model.NearbyMatchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MapApiService {
    @POST("api/match/nearby")
    suspend fun getNearbyMatches(
        @Body query: LocationQueryDto
    ): Response<List<NearbyMatchResponse>>

    @POST("api/match/create")
    suspend fun createMatch(
        @Body matchData: CreateMatchDto
    ): Response<Unit>
}
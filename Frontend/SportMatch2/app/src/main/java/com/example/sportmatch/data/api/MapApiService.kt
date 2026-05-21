package com.example.sportmatch.data.api

import com.example.sportmatch.data.dto.ApplyMatchDto
import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.CreateMatchDto
import com.example.sportmatch.data.dto.LocationQueryDto
import com.example.sportmatch.data.dto.MyRequestDto
import com.example.sportmatch.data.dto.NearbyMatchResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MapApiService {
    @POST("api/match/nearby")
    suspend fun getNearbyMatches(
        @Body query: LocationQueryDto
    ): Response<List<NearbyMatchResponseDto>>

    @POST("api/match/create")
    suspend fun createMatch(
        @Body matchData: CreateMatchDto
    ): Response<Unit>

    @POST("api/match/apply")
    suspend fun applyForMatch(
        @Body request: ApplyMatchDto
    ): Response<BaseResponseDto>

    @GET("api/match/my-requests/{userId}")
    suspend fun getMyRequests(@Path("userId") userId: Int): Response<List<MyRequestDto>>

    @DELETE("api/match/cancel-request/{interactionId}")
    suspend fun cancelRequest(@Path("interactionId") interactionId: Int): Response<BaseResponseDto>
}
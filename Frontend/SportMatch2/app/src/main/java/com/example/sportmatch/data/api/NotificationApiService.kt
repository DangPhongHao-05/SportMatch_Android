package com.example.sportmatch.data.api

import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.NotificationResponseDto
import com.example.sportmatch.data.dto.RespondRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationApiService {

    // API lấy danh sách thông báo chờ duyệt
    @GET("api/match/notifications/{hostId}")
    suspend fun getNotifications(
        @Path("hostId") hostId: Int
    ): Response<List<NotificationResponseDto>>

    // API duyệt đơn (Đồng ý / Từ chối)
    @PATCH("api/match/respond")
    suspend fun respondToMatchRequest(
        @Body request: RespondRequestDto
    ): Response<BaseResponseDto>
}
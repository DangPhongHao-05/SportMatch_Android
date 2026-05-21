package com.example.sportmatch.data.repository

import com.example.sportmatch.data.api.RetrofitClient
import com.example.sportmatch.data.dto.BaseResponseDto
import com.example.sportmatch.data.dto.MyRequestDto
import com.example.sportmatch.data.dto.NotificationResponseDto
import com.example.sportmatch.data.dto.RespondRequestDto
import retrofit2.Response

class NotificationRepository {
    private val apiService = RetrofitClient.notificationApi

    // 1. Lấy danh sách thông báo về (Chờ mình duyệt)
    suspend fun getNotifications(hostId: Int): Response<List<NotificationResponseDto>> {
        return apiService.getNotifications(hostId)
    }

    // 2. Gửi quyết định ok/Từ chối đi
    suspend fun respondToRequest(request: RespondRequestDto): Response<BaseResponseDto> {
        return apiService.respondToMatchRequest(request)
    }

    // 3. Lấy danh sách đơn mình gửi đi
    suspend fun getMyRequests(userId: Int): Response<List<MyRequestDto>> {
        return RetrofitClient.mapApi.getMyRequests(userId)
    }

    // 4. Hủy đơn đã gửi
    suspend fun cancelRequest(interactionId: Int): Response<BaseResponseDto> {
        return RetrofitClient.mapApi.cancelRequest(interactionId)
    }
}
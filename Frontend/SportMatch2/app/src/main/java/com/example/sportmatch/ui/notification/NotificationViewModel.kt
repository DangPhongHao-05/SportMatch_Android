package com.example.sportmatch.ui.notification

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportmatch.data.dto.MyRequestDto
import com.example.sportmatch.data.dto.NotificationResponseDto
import com.example.sportmatch.data.dto.RespondRequestDto
import com.example.sportmatch.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    // TAB 1: CHỜ DUYỆT
    val notifications = mutableStateListOf<NotificationResponseDto>()

    fun fetchNotifications(hostId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getNotifications(hostId)
                if (response.isSuccessful && response.body() != null) {
                    notifications.clear()
                    notifications.addAll(response.body()!!)
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }

    fun respondToRequest(interactionId: Int, isAccepted: Boolean, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val dto = RespondRequestDto(interactionId, isAccepted)
                val response = repository.respondToRequest(dto)

                if (response.isSuccessful && response.body() != null) {
                    val baseResponse = response.body()!!
                    if (baseResponse.success) {
                        val index = notifications.indexOfFirst { it.interactionId == interactionId }
                        if (index != -1) {
                            val currentNotif = notifications[index]
                            notifications[index] = currentNotif.copy(
                                status = if (isAccepted) "Accepted" else "Rejected"
                            )
                        }
                        onResult(baseResponse.message)
                    }
                } else {
                    onResult("Xử lý thất bại, vui lòng thử lại!")
                }
            } catch (e: Exception) {
                onResult("Lỗi kết nối Server!")
            }
        }
    }

    // TAB 2: ĐƠN MÌNH GỬI
    val myRequests = mutableStateListOf<MyRequestDto>()

    fun fetchMyRequests(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getMyRequests(userId) // Gọi repository
                if (response.isSuccessful && response.body() != null) {
                    myRequests.clear()
                    myRequests.addAll(response.body()!!)
                }
            } catch (e: Exception) {
                // Lỗi mạng
            }
        }
    }

    fun cancelMyRequest(interactionId: Int, userId: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.cancelRequest(interactionId) // Gọi repository
                if (response.isSuccessful && response.body() != null) {
                    val baseResponse = response.body()!!
                    if (baseResponse.success) {
                        onResult("Đã hủy yêu cầu thành công!")
                        fetchMyRequests(userId) // Load lại danh sách sau khi hủy
                    } else {
                        onResult(baseResponse.message)
                    }
                } else {
                    onResult("Hủy thất bại!")
                }
            } catch (e: Exception) {
                onResult("Lỗi kết nối Server!")
            }
        }
    }
}
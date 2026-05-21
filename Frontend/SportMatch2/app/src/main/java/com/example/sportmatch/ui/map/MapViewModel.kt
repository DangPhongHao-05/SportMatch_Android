package com.example.sportmatch.ui.match

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportmatch.data.repository.MapRepository
import kotlinx.coroutines.launch
import com.example.sportmatch.data.dto.CreateMatchDto
import com.example.sportmatch.data.dto.NearbyMatchResponseDto

class MapViewModel : ViewModel() {
    private val repository = MapRepository()

    // Danh sách "kèo" thật sẽ được Compose quan sát để vẽ lên Map
    val nearbyMatches = mutableStateListOf<NearbyMatchResponseDto>()

    fun fetchNearbyMatches(lat: Double, lng: Double, radiusInKm: Double = 5.0) {
        viewModelScope.launch {
            try {
                val response = repository.getNearbyMatches(lat, lng, radiusInKm)
                if (response.isSuccessful && response.body() != null) {
                    nearbyMatches.clear()
                    nearbyMatches.addAll(response.body()!!)
                    Log.d("MAP_DATA", "Đã tải thành công ${nearbyMatches.size} trận đấu từ Backend.")
                } else {
                    Log.e("MAP_DATA_ERROR", "Lỗi API: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MAP_DATA_ERROR", "Lỗi kết nối mạng: ${e.message}")
            }
        }
    }

    fun createNewMatch(matchData: CreateMatchDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createMatch(matchData)
                if (response.isSuccessful) {
                    Log.d("MAP_DATA", "Đăng kèo thành công lên Database MySQL!")

                    // Chạy hàm callback đóng BottomSheet bên giao diện
                    onSuccess()

                    // Tự động load lại các ghim xung quanh vị trí vừa đăng để cập nhật UI ngay lập tức
                    fetchNearbyMatches(matchData.latitude, matchData.longitude)
                } else {
                    Log.e("MAP_DATA_ERROR", "Đăng kèo thất bại từ server: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MAP_DATA_ERROR", "Lỗi kết nối mạng khi tạo trận: ${e.message}")
            }
        }
    }

    // HÀM GỬI YÊU CẦU XIN THAM GIA
    fun sendApplyRequest(matchId: Int, currentUserId: Int, message: String, onResult: (String, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Đóng gói dữ liệu vào DTO
                val dto = com.example.sportmatch.data.dto.ApplyMatchDto(
                    matchRequestId = matchId,
                    userId = currentUserId,
                    message = message
                )

                // 2. Gọi qua tầng Repository của bạn
                val response = repository.applyForMatch(dto)

                if (response.isSuccessful && response.body() != null) {
                    // Crả về mã 200 OK thành công
                    onResult(response.body()!!.message, true)
                } else {
                    // trả về mã 400 BadRequest (Chống Spam)
                    val errorJson = response.errorBody()?.string()
                    val errorResponse = try {
                        com.google.gson.Gson().fromJson(
                            errorJson,
                            com.example.sportmatch.data.dto.BaseResponseDto::class.java
                        )
                    } catch (e: Exception) { null }

                    val finalMsg = errorResponse?.message ?: "Bạn đã gửi đơn xin vào trận này rồi, không được spam!"
                    onResult(finalMsg, false)
                }
            } catch (e: Exception) {
                Log.e("MAP_DATA_ERROR", "Lỗi mạng khi gửi yêu cầu tham gia: ${e.message}")
                onResult("Lỗi kết nối Server!", false)
            }
        }
    }

}
package com.example.sportmatch.ui.match

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportmatch.data.model.NearbyMatchResponse
import com.example.sportmatch.data.repository.MapRepository
import kotlinx.coroutines.launch
import com.example.sportmatch.data.dto.CreateMatchDto

class MapViewModel : ViewModel() {
    private val repository = MapRepository()

    // Danh sách "kèo" thật sẽ được Compose quan sát để vẽ lên Map
    val nearbyMatches = mutableStateListOf<NearbyMatchResponse>()

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

}
package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

class LogoutRequestDto (
    @SerializedName("userId") val userId: Int
)
package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

class UserDto (
    @SerializedName("id") val id: Int,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?
)
package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

class AuthResponseDto (
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)
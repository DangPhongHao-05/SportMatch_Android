package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

class UploadAvatarDto (
    @SerializedName("message") val message: String,
    @SerializedName("avatarUrl") val avatarUrl: String
)
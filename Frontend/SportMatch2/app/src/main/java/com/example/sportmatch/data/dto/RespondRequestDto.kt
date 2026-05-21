package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

data class RespondRequestDto(
    @SerializedName("interactionId")
    val interactionId: Int,
    @SerializedName("isAccepted")
    val isAccepted: Boolean
)
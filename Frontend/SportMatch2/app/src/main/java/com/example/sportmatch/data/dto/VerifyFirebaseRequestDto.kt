package com.example.sportmatch.data.dto

import com.google.gson.annotations.SerializedName

class VerifyFirebaseRequestDto (
    @SerializedName("idToken") val idToken: String
)
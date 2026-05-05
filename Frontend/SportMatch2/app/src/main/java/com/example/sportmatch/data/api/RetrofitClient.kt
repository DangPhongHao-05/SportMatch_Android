package com.example.sportmatch.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // đổi IP nếu bạn test trên máy thật (VD: 192.168.x.x)
    private const val BASE_URL = "http://10.0.2.2:5020/"

    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
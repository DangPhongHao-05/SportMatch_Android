package com.example.sportmatch.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // đổi IP nếu bạn test trên máy thật (VD: 192.168.x.x)
    private const val BASE_URL = "http://10.0.2.2:5020/"
//    private const val BASE_URL = "http://192.168.1.5:5020/"
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val mapApi: MapApiService by lazy {
        retrofit.create(MapApiService::class.java)
    }

    val notificationApi: NotificationApiService by lazy {
        retrofit.create(NotificationApiService::class.java)
    }

}
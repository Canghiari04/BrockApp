package com.example.brockapp.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientInstance {
    companion object {
        private const val BASE_URL = "https://wft-geo-db.p.rapidapi.com/v1/geo/"

        val api: GeoApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeoApiService::class.java)
        }
    }
}
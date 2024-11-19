package com.example.brockapp.retrofit

import com.example.brockapp.BuildConfig

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientInstance {

    companion object {
        val api: GeoApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BuildConfig.GEO_DB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeoApiService::class.java)
        }
    }
}
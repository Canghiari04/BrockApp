package com.example.brockapp.retrofit

import com.example.brockapp.data.CityResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface GeoApiService {
    @GET("cities")
    fun getCitiesByCountryId(
        @Header("x-RapidApi-key") apiKey: String,
        @Query("countryIds") countryCode: String
    ): Call<CityResponse>
}
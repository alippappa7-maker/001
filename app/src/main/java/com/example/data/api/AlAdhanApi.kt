package com.example.data.api

import com.example.data.api.model.AlAdhanResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AlAdhanApi {
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") date: String, // DD-MM-YYYY
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0,
        @Query("tune") tune: String? = null,
        @Query("timezone") timezone: String? = null
    ): AlAdhanResponse
}

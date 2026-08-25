package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val AL_ADHAN_BASE_URL = "https://api.aladhan.com/"
    private const val QURAN_BASE_URL = "https://api.quran.com/api/v4/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val alAdhanApi: AlAdhanApi by lazy {
        Retrofit.Builder()
            .baseUrl(AL_ADHAN_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlAdhanApi::class.java)
    }

    val quranApi: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(QURAN_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranApiService::class.java)
    }
}

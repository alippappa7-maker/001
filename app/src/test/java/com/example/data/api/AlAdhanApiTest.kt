package com.example.data.api

import com.example.data.api.model.AlAdhanResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AlAdhanApiTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val responseAdapter = moshi.adapter(AlAdhanResponse::class.java)

    @Test
    fun testParseAlAdhanResponse() {
        // Sample JSON response similar to what AlAdhan returns
        val jsonStr = """
        {
            "code": 200,
            "status": "OK",
            "data": {
                "timings": {
                    "Fajr": "04:30",
                    "Sunrise": "05:45",
                    "Dhuhr": "12:15",
                    "Asr": "15:45",
                    "Sunset": "18:45",
                    "Maghrib": "18:45",
                    "Isha": "20:00",
                    "Imsak": "04:20",
                    "Midnight": "00:00"
                },
                "date": {
                    "readable": "01 Jan 2024",
                    "timestamp": "1704067200",
                    "hijri": {
                        "date": "19-06-1445",
                        "format": "DD-MM-YYYY",
                        "day": "19",
                        "weekday": {
                            "en": "Al Jumu'ah",
                            "ar": "الجمعة"
                        },
                        "month": {
                            "number": 6,
                            "en": "Jumādá al-ākhirah",
                            "ar": "جُمادى الآخرة"
                        },
                        "year": "1445"
                    },
                    "gregorian": {
                        "date": "01-01-2024",
                        "format": "DD-MM-YYYY",
                        "day": "01",
                        "weekday": {
                            "en": "Monday"
                        },
                        "month": {
                            "number": 1,
                            "en": "January"
                        },
                        "year": "2024"
                    }
                },
                "meta": {
                    "latitude": 36.035,
                    "longitude": 37.452,
                    "timezone": "Asia/Damascus",
                    "method": {
                        "id": 2,
                        "name": "Islamic Society of North America (ISNA)"
                    }
                }
            }
        }
        """.trimIndent()

        val response = responseAdapter.fromJson(jsonStr)

        assertNotNull(response)
        assertEquals(200, response?.code)
        assertEquals("04:30", response?.data?.timings?.fajr)
        assertEquals("15:45", response?.data?.timings?.asr)
        assertEquals("01-01-2024", response?.data?.date?.gregorian?.date)
        assertEquals("1445", response?.data?.date?.hijri?.year)
        assertEquals("جُمادى الآخرة", response?.data?.date?.hijri?.month?.ar)
    }
}

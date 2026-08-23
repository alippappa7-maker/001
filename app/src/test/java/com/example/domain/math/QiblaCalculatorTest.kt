package com.example.domain.math

import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaCalculatorTest {

    @Test
    fun testQiblaCalculation_FromCairo() {
        // Cairo: ~30.0444, 31.2357 -> Qibla should be around 136 degrees (South East)
        val lat = 30.0444
        val lng = 31.2357
        val bearing = QiblaCalculator.calculateQiblaBearing(lat, lng)
        assertEquals(136.4, bearing.toDouble(), 0.5) // Allowing 0.5 deg tolerance
    }

    @Test
    fun testQiblaCalculation_FromNewYork() {
        // New York: ~40.7128, -74.0060 -> Qibla should be around 58.5 degrees (North East)
        val lat = 40.7128
        val lng = -74.0060
        val bearing = QiblaCalculator.calculateQiblaBearing(lat, lng)
        assertEquals(58.5, bearing.toDouble(), 0.5)
    }

    @Test
    fun testQiblaCalculation_FromTokyo() {
        // Tokyo: ~35.6762, 139.6503 -> Qibla should be around 293 degrees (North West)
        val lat = 35.6762
        val lng = 139.6503
        val bearing = QiblaCalculator.calculateQiblaBearing(lat, lng)
        assertEquals(293.0, bearing.toDouble(), 0.5)
    }
}

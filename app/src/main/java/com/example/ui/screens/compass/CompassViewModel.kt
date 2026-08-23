package com.example.ui.screens.compass

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.math.QiblaCalculator
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class CompassUiState(
    val qiblaAngle: Float? = null,
    val userAngle: Float = 0f,
    val isLocationPermissionGranted: Boolean = false,
    val isLocationEnabled: Boolean = true,
    val location: Location? = null,
    val sensorAccuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val isCalibrating: Boolean = false,
    val manualCityMode: Boolean = false
)

class CompassViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private var isListeningToSensors = false
    private var isListeningToLocation = false
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { updateLocation(it) }
        }
    }

    fun onPermissionGranted() {
        _uiState.update { it.copy(isLocationPermissionGranted = true, manualCityMode = false) }
        startLocationUpdates()
        startSensors()
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(isLocationPermissionGranted = false) }
    }

    fun enableManualCityMode() {
        _uiState.update { it.copy(manualCityMode = true) }
        // Default to a fallback (e.g., Mecca itself or let the user enter. We will simulate Mecca or Riyadh if they choose manual for now)
        // In a full app, this would be a real geocoding search.
        val fallbackLocation = Location("manual").apply {
            latitude = 24.7136 // Riyadh
            longitude = 46.6753
        }
        updateLocation(fallbackLocation)
        startSensors()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (isListeningToLocation) return
        
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let { updateLocation(it) }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        isListeningToLocation = true
    }

    private fun updateLocation(loc: Location) {
        val qiblaAngle = QiblaCalculator.calculateQiblaBearing(loc.latitude, loc.longitude)
        _uiState.update { it.copy(location = loc, qiblaAngle = qiblaAngle) }
    }

    fun startSensors() {
        if (isListeningToSensors || rotationSensor == null) return
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        isListeningToSensors = true
    }

    fun stopSensors() {
        if (!isListeningToSensors) return
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isListeningToSensors = false
        isListeningToLocation = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthInRadians = orientation[0]
            var azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
            azimuthInDegrees = (azimuthInDegrees + 360) % 360
            
            _uiState.update { it.copy(userAngle = azimuthInDegrees) }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            // Fallback for simple magnetic field if rotation vector is unavailable.
            // A proper implementation would combine TYPE_ACCELEROMETER as well.
            // For brevity, assuming rotation vector is usually available on modern devices.
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _uiState.update {
            it.copy(
                sensorAccuracy = accuracy,
                isCalibrating = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}

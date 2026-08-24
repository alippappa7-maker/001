package com.example

import android.app.Application
import android.util.Log
import com.example.data.firebase.FirebaseSafeManager
import com.google.firebase.FirebaseApp

class QabasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            val configured = FirebaseSafeManager.isConfigured(this)
            Log.i(TAG, "QabasApplication started. Firebase configured: $configured")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization notice: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "QabasApplication"
    }
}

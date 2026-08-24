package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSafeManager {
    private const val TAG = "FirebaseSafeManager"

    fun isConfigured(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase is not configured or initialized: ${e.message}")
            false
        }
    }

    fun getAuth(context: Context): FirebaseAuth? {
        if (!isConfigured(context)) return null
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not available: ${e.message}")
            null
        }
    }

    fun getFirestore(context: Context): FirebaseFirestore? {
        if (!isConfigured(context)) return null
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore not available: ${e.message}")
            null
        }
    }
}

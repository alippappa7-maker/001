package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.firebase.FirebaseSafeManager
import com.example.domain.model.UserAccount
import com.example.domain.model.UserDataSync
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

interface UserRepository {
    val currentUser: Flow<UserAccount?>
    val isFirebaseAvailable: Boolean
    
    suspend fun signInWithEmail(email: String, password: String): Result<UserAccount>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String?): Result<UserAccount>
    suspend fun signInWithGoogleCredential(idToken: String): Result<UserAccount>
    suspend fun launchGoogleSignIn(context: Context, webClientId: String): Result<UserAccount>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>

    suspend fun syncUserData(userData: UserDataSync): Result<Unit>
    suspend fun fetchCloudUserData(): Result<UserDataSync?>
    fun observeCloudUserData(): Flow<UserDataSync?>
}

class UserRepositoryImpl(
    private val context: Context
) : UserRepository {

    private val tag = "UserRepositoryImpl"
    private val auth: FirebaseAuth? get() = FirebaseSafeManager.getAuth(context)
    private val firestore: FirebaseFirestore? get() = FirebaseSafeManager.getFirestore(context)

    override val isFirebaseAvailable: Boolean
        get() = FirebaseSafeManager.isConfigured(context)

    override val currentUser: Flow<UserAccount?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            val firebaseUser = currentAuth.currentUser
            if (firebaseUser == null) {
                firestoreListener?.remove()
                firestoreListener = null
                trySend(null)
            } else {
                trySend(firebaseUser.toUserAccount())
                val db = firestore
                if (db != null && firestoreListener == null) {
                    firestoreListener = db.collection("users").document(firebaseUser.uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                            try {
                                val roleStr = snapshot.getString("role") ?: "USER"
                                val statusStr = snapshot.getString("status") ?: "ACTIVE"
                                val role = enumValueOf<com.example.domain.model.UserRole>(roleStr)
                                val status = enumValueOf<com.example.domain.model.AccountStatus>(statusStr)
                                trySend(firebaseUser.toUserAccount().copy(role = role, status = status))
                            } catch (e: Exception) {
                                // Ignore parsing errors
                            }
                        }
                }
            }
        }

        firebaseAuth.addAuthStateListener(listener)
        // Initial state is handled by the AuthStateListener callback

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
            firestoreListener?.remove()
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserAccount> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Firebase is not initialized. Running in local mode.")
        )
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user?.toUserAccount()
                ?: return Result.failure(IllegalStateException("Failed to retrieve user"))
            
            // Record last login in Firestore (without sensitive credentials)
            updateUserLoginTimestamp(user.uid)

            // Auto-promote developer to SUPER_ADMIN on login if matches
            if (user.email == "aliwalead.2007@gmail.com") {
                try {
                    firestore?.collection("users")?.document(user.uid)?.set(
                        hashMapOf("role" to "SUPER_ADMIN", "status" to "ACTIVE"),
                        SetOptions.merge()
                    )?.await()
                } catch (e: Exception) {
                    Log.w(tag, "Could not auto-promote developer: ${e.message}")
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e(tag, "Sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<UserAccount> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Firebase is not initialized. Running in local mode.")
        )
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Failed to create user"))

            if (!displayName.isNullOrBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }

            val user = firebaseUser.toUserAccount().copy(
                displayName = displayName?.trim() ?: firebaseUser.displayName
            )

            // Initialize user doc in Firestore (no passwords, only profile)
            initUserDocInFirestore(user)

            Result.success(user)
        } catch (e: Exception) {
            Log.e(tag, "Sign up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleCredential(idToken: String): Result<UserAccount> {
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Firebase is not initialized. Running in local mode.")
        )
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user?.toUserAccount()
                ?: return Result.failure(IllegalStateException("Failed to sign in with Google"))
            
            updateUserLoginTimestamp(user.uid)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(tag, "Google credential sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun launchGoogleSignIn(context: Context, webClientId: String): Result<UserAccount> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                signInWithGoogleCredential(googleIdTokenCredential.idToken)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth?.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Sign out error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val currentUser = auth?.currentUser ?: return Result.failure(
            IllegalStateException("No active user session found")
        )
        val uid = currentUser.uid
        return try {
            // Delete Firestore user data first
            firestore?.collection("users")?.document(uid)?.delete()?.await()
            // Delete Auth account
            currentUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Delete account error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncUserData(userData: UserDataSync): Result<Unit> {
        val currentUser = auth?.currentUser ?: return Result.failure(
            IllegalStateException("User is not authenticated. Skipping cloud sync.")
        )
        val db = firestore ?: return Result.failure(
            IllegalStateException("Firestore is not available.")
        )

        return try {
            val syncMap = hashMapOf<String, Any?>(
                "favoriteZikrIds" to userData.favoriteZikrIds,
                "dailyCompletedTasks" to userData.dailyCompletedTasks,
                "dailyTotalTasks" to userData.dailyTotalTasks,
                "isDarkMode" to userData.isDarkMode,
                "language" to userData.language,
                "selectedCityId" to userData.selectedCityId,
                "lastSyncedAt" to System.currentTimeMillis()
            )

            db.collection("users")
                .document(currentUser.uid)
                .set(syncMap, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Cloud sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchCloudUserData(): Result<UserDataSync?> {
        val currentUser = auth?.currentUser ?: return Result.failure(
            IllegalStateException("User is not authenticated.")
        )
        val db = firestore ?: return Result.failure(
            IllegalStateException("Firestore is not available.")
        )

        return try {
            val snapshot = db.collection("users").document(currentUser.uid).get().await()
            if (!snapshot.exists()) {
                return Result.success(null)
            }

            @Suppress("UNCHECKED_CAST")
            val favs = (snapshot.get("favoriteZikrIds") as? List<String>) ?: emptyList()
            val completed = (snapshot.getLong("dailyCompletedTasks") ?: 0L).toInt()
            val total = (snapshot.getLong("dailyTotalTasks") ?: 5L).toInt()
            val isDark = snapshot.getBoolean("isDarkMode")
            val lang = snapshot.getString("language")
            val cityId = snapshot.getString("selectedCityId")
            val syncedAt = snapshot.getLong("lastSyncedAt") ?: System.currentTimeMillis()

            val data = UserDataSync(
                favoriteZikrIds = favs,
                dailyCompletedTasks = completed,
                dailyTotalTasks = total,
                isDarkMode = isDark,
                language = lang,
                selectedCityId = cityId,
                lastSyncedAt = syncedAt
            )
            Result.success(data)
        } catch (e: Exception) {
            Log.e(tag, "Fetch cloud data failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun observeCloudUserData(): Flow<UserDataSync?> = callbackFlow {
        val currentUser = auth?.currentUser
        val db = firestore

        if (currentUser == null || db == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("users")
            .document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                @Suppress("UNCHECKED_CAST")
                val favs = (snapshot.get("favoriteZikrIds") as? List<String>) ?: emptyList()
                val completed = (snapshot.getLong("dailyCompletedTasks") ?: 0L).toInt()
                val total = (snapshot.getLong("dailyTotalTasks") ?: 5L).toInt()
                val isDark = snapshot.getBoolean("isDarkMode")
                val lang = snapshot.getString("language")
                val cityId = snapshot.getString("selectedCityId")
                val syncedAt = snapshot.getLong("lastSyncedAt") ?: System.currentTimeMillis()

                trySend(
                    UserDataSync(
                        favoriteZikrIds = favs,
                        dailyCompletedTasks = completed,
                        dailyTotalTasks = total,
                        isDarkMode = isDark,
                        language = lang,
                        selectedCityId = cityId,
                        lastSyncedAt = syncedAt
                    )
                )
            }

        awaitClose {
            registration.remove()
        }
    }

    private suspend fun updateUserLoginTimestamp(uid: String) {
        try {
            val db = firestore ?: return
            val update = hashMapOf<String, Any>(
                "lastLoginAt" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).set(update, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(tag, "Could not update login timestamp: ${e.message}")
        }
    }

    private suspend fun initUserDocInFirestore(user: UserAccount) {
        try {
            val db = firestore ?: return
            val roleStr = if (user.email == "aliwalead.2007@gmail.com") "SUPER_ADMIN" else "USER"
            val userMap = hashMapOf<String, Any?>(
                "uid" to user.uid,
                "email" to user.email,
                "displayName" to user.displayName,
                "createdAt" to user.createdAt,
                "lastLoginAt" to user.lastLoginAt,
                "role" to roleStr,
                "status" to "ACTIVE"
            )
            db.collection("users").document(user.uid).set(userMap, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(tag, "Could not initialize user doc in Firestore: ${e.message}")
        }
    }
}

private fun FirebaseUser.toUserAccount(): UserAccount {
    return UserAccount(
        uid = this.uid,
        email = this.email,
        displayName = this.displayName,
        photoUrl = this.photoUrl?.toString(),
        isAnonymous = this.isAnonymous,
        createdAt = this.metadata?.creationTimestamp ?: System.currentTimeMillis(),
        lastLoginAt = this.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
    )
}

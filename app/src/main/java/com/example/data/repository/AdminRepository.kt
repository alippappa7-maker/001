package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseSafeManager
import com.example.domain.model.AccountStatus
import com.example.domain.model.UserAccount
import com.example.domain.model.UserRole
import com.example.domain.model.admin.AuditLog
import com.example.domain.model.admin.IssueDiagnosis
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface AdminRepository {
    fun observeAllUsers(): Flow<List<UserAccount>>
    suspend fun updateUserStatus(targetUid: String, newStatus: AccountStatus, reason: String, actorUid: String, actorRole: UserRole): Result<Unit>
    suspend fun updateUserRole(targetUid: String, newRole: UserRole, reason: String, actorUid: String, actorRole: UserRole): Result<Unit>
    fun observeAuditLogs(): Flow<List<AuditLog>>
    fun observeDiagnostics(): Flow<List<IssueDiagnosis>>
}

class AdminRepositoryImpl(
    private val context: Context
) : AdminRepository {
    private val tag = "AdminRepositoryImpl"
    private val firestore: FirebaseFirestore? get() = FirebaseSafeManager.getFirestore(context)

    override fun observeAllUsers(): Flow<List<UserAccount>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val registration = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(tag, "Error observing users", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        UserAccount(
                            uid = doc.getString("uid") ?: doc.id,
                            email = doc.getString("email"),
                            displayName = doc.getString("displayName"),
                            photoUrl = doc.getString("photoUrl"),
                            isAnonymous = doc.getBoolean("isAnonymous") ?: false,
                            role = doc.getString("role")?.let { enumValueOf<UserRole>(it) } ?: UserRole.USER,
                            status = doc.getString("status")?.let { enumValueOf<AccountStatus>(it) } ?: AccountStatus.ACTIVE,
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            lastLoginAt = doc.getLong("lastLoginAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(users)
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun updateUserStatus(targetUid: String, newStatus: AccountStatus, reason: String, actorUid: String, actorRole: UserRole): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            db.collection("users").document(targetUid)
                .set(mapOf("status" to newStatus.name), SetOptions.merge()).await()
            logAction(db, actorUid, actorRole, "UPDATE_STATUS", targetUid, reason, mapOf("newStatus" to newStatus.name))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(targetUid: String, newRole: UserRole, reason: String, actorUid: String, actorRole: UserRole): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            db.collection("users").document(targetUid)
                .set(mapOf("role" to newRole.name), SetOptions.merge()).await()
            logAction(db, actorUid, actorRole, "UPDATE_ROLE", targetUid, reason, mapOf("newRole" to newRole.name))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = db.collection("audit_logs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val logs = snapshot.documents.mapNotNull { doc ->
                    try {
                        AuditLog(
                            logId = doc.id,
                            actorId = doc.getString("actorId") ?: "",
                            actorRole = enumValueOf<UserRole>(doc.getString("actorRole") ?: "USER"),
                            action = doc.getString("action") ?: "",
                            targetId = doc.getString("targetId"),
                            reason = doc.getString("reason") ?: "",
                            metadata = (doc.get("metadata") as? Map<String, String>) ?: emptyMap(),
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(logs)
            }
        awaitClose { registration.remove() }
    }

    override fun observeDiagnostics(): Flow<List<IssueDiagnosis>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = db.collection("diagnostics")
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val diagnostics = snapshot.documents.mapNotNull { doc ->
                    try {
                        IssueDiagnosis(
                            issueId = doc.id,
                            title = doc.getString("title") ?: "",
                            summary = doc.getString("summary") ?: "",
                            technicalCause = doc.getString("technicalCause") ?: "",
                            affectedComponent = doc.getString("affectedComponent"),
                            severity = enumValueOf(doc.getString("severity") ?: "MODERATE"),
                            affectedUsers = doc.getLong("affectedUsers")?.toInt() ?: 1,
                            reproductionSteps = doc.getString("reproductionSteps") ?: "",
                            suggestedFix = doc.getString("suggestedFix") ?: "",
                            confidence = doc.getDouble("confidence")?.toFloat() ?: 0.5f,
                            status = enumValueOf(doc.getString("status") ?: "NEW"),
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(diagnostics)
            }
        awaitClose { registration.remove() }
    }

    private suspend fun logAction(db: FirebaseFirestore, actorId: String, actorRole: UserRole, action: String, targetId: String?, reason: String, metadata: Map<String, String>) {
        try {
            val log = AuditLog(
                logId = UUID.randomUUID().toString(),
                actorId = actorId,
                actorRole = actorRole,
                action = action,
                targetId = targetId,
                reason = reason,
                metadata = metadata,
                timestamp = System.currentTimeMillis()
            )
            db.collection("audit_logs").document(log.logId).set(log).await()
        } catch (e: Exception) {
            Log.e(tag, "Failed to write audit log", e)
        }
    }
}

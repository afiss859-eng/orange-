package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val actorName: String,
    val actionType: String, // e.g. "CONNEXION", "TRANSACTION", "AJUSTEMENT_SOLDE", "EXPORT_CSV", "SUPPRESSION_CLIENT"
    val description: String,
    val details: String = ""
)

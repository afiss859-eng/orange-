package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus(val displayName: String) {
    SYNCED("Synchronisé"),
    PENDING("En attente"),
    OFFLINE("Local (Hors-ligne)")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val network: String = "ORANGE_MONEY", // ORANGE_MONEY
    val type: String = "RETRAIT", // DEPOT, RETRAIT, TRANSFERT, FACTURE, CREDIT
    val amount: Double = 0.0,
    val fee: Double = 0.0,
    val clientPhone: String = "",
    val recipientPhone: String = "",
    val clientName: String = "",
    val birthDate: String? = null,
    val idType: String = "CNIB",
    val idNumber: String = "",
    val idPhotoPath: String? = null,
    val kioskBalanceBefore: Double = 0.0,
    val kioskBalanceAfter: Double = 0.0,
    val cashBalanceBefore: Double = 0.0,
    val cashBalanceAfter: Double = 0.0,
    val referenceCode: String = "",
    val notes: String = "",
    val agentName: String = "Agent Gérant",
    val syncStatus: String = SyncStatus.OFFLINE.name,
    val isVerified: Boolean = true
)

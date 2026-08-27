package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey
    val phone: String,
    val fullName: String = "",
    val idType: String = "CNIB",
    val idNumber: String = "",
    val idPhotoPath: String? = null,
    val birthDate: String? = null,
    val lastTransactionTimestamp: Long = System.currentTimeMillis(),
    val transactionCount: Int = 1,
    val totalVolume: Double = 0.0,
    val notes: String = ""
)

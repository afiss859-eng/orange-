package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val level: Int) {
    ADMIN("Administrateur", 3),     // Full access to all settings, agents, audit logs
    RESPONSABLE("Responsable", 2),  // Access to operations, caisse, statistics, history
    AGENT("Agent / Caissier", 1)    // Access to new operations, clients, and receipts
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val username: String,
    val fullName: String,
    val phone: String = "",
    val pinCode: String = "1234",
    val role: UserRole = UserRole.AGENT,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)

package com.example.data.repository

import com.example.data.db.AuditLogDao
import com.example.data.db.ClientDao
import com.example.data.db.TransactionDao
import com.example.data.db.UserDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.ClientEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

class KioskRepository(
    private val transactionDao: TransactionDao,
    private val clientDao: ClientDao,
    private val userDao: UserDao,
    private val auditLogDao: AuditLogDao,
    val preferences: KioskPreferences
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(query)

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(start, end)

    fun searchClients(query: String): Flow<List<ClientEntity>> =
        clientDao.searchClients(query)

    suspend fun findClientByPhone(phone: String): ClientEntity? =
        clientDao.getClientByPhone(phone.trim())

    suspend fun findClientByIdNumber(idNumber: String): ClientEntity? =
        clientDao.getClientByIdNumber(idNumber.trim())

    suspend fun saveTransaction(
        transaction: TransactionEntity,
        updateKioskBalances: Boolean = true
    ): Long {
        val id = transactionDao.insertTransaction(transaction)

        // Update or insert client into VIP directory
        if (transaction.clientPhone.isNotBlank()) {
            val existing = clientDao.getClientByPhone(transaction.clientPhone)
            val updatedClient = if (existing != null) {
                existing.copy(
                    fullName = if (transaction.clientName.isNotBlank()) transaction.clientName else existing.fullName,
                    idType = if (transaction.idNumber.isNotBlank()) transaction.idType else existing.idType,
                    idNumber = if (transaction.idNumber.isNotBlank()) transaction.idNumber else existing.idNumber,
                    idPhotoPath = transaction.idPhotoPath ?: existing.idPhotoPath,
                    birthDate = if (!transaction.birthDate.isNullOrBlank()) transaction.birthDate else existing.birthDate,
                    lastTransactionTimestamp = transaction.timestamp,
                    transactionCount = existing.transactionCount + 1,
                    totalVolume = existing.totalVolume + transaction.amount
                )
            } else {
                ClientEntity(
                    phone = transaction.clientPhone,
                    fullName = transaction.clientName,
                    idType = transaction.idType,
                    idNumber = transaction.idNumber,
                    idPhotoPath = transaction.idPhotoPath,
                    birthDate = transaction.birthDate,
                    lastTransactionTimestamp = transaction.timestamp,
                    transactionCount = 1,
                    totalVolume = transaction.amount
                )
            }
            clientDao.insertClient(updatedClient)
        }

        // Apply automatic balance updates to kiosk cash & e-money
        if (updateKioskBalances) {
            val isCashIn = transaction.type == "DEPOT" || transaction.type == "TRANSFERT" || transaction.type == "FACTURE"
            preferences.applyTransactionBalances(
                network = transaction.network,
                isCashIn = isCashIn,
                amount = transaction.amount,
                fee = transaction.fee
            )
        }

        // Log action in audit trail
        logAudit(
            actor = transaction.agentName,
            actionType = "TRANSACTION",
            description = "${transaction.type} de ${transaction.amount.toInt()} FCFA via ${transaction.network}",
            details = "Client: ${transaction.clientName} (${transaction.clientPhone}), Solde après: ${transaction.kioskBalanceAfter.toInt()} FCFA"
        )

        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity, actor: String = "Admin") {
        transactionDao.deleteTransaction(transaction)
        logAudit(
            actor = actor,
            actionType = "SUPPRESSION_TRANSACTION",
            description = "Suppression transaction ID ${transaction.id}",
            details = "Montant: ${transaction.amount} FCFA (${transaction.type})"
        )
    }

    suspend fun deleteClient(phone: String, actor: String = "Admin") {
        clientDao.deleteClientByPhone(phone)
        logAudit(
            actor = actor,
            actionType = "SUPPRESSION_CLIENT",
            description = "Suppression fiche client $phone",
            details = ""
        )
    }

    suspend fun saveUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    suspend fun logAudit(actor: String, actionType: String, description: String, details: String = "") {
        try {
            auditLogDao.insertLog(
                AuditLogEntity(
                    actorName = actor,
                    actionType = actionType,
                    description = description,
                    details = details
                )
            )
        } catch (e: Exception) {
            // Ignore audit log write failure
        }
    }
}

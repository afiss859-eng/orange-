package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE clientPhone LIKE '%' || :query || '%' OR clientName LIKE '%' || :query || '%' OR idNumber LIKE '%' || :query || '%' OR referenceCode LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE clientPhone = :phone ORDER BY timestamp DESC")
    fun getTransactionsForClient(phone: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND timestamp >= :startTime")
    fun getSumByTypeSince(type: String, startTime: Long): Flow<Double?>

    @Query("SELECT SUM(fee) FROM transactions WHERE timestamp >= :startTime")
    fun getTotalCommissionsSince(startTime: Long): Flow<Double?>
}

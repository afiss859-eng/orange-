package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Query("SELECT * FROM clients ORDER BY lastTransactionTimestamp DESC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE phone LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%' OR idNumber LIKE '%' || :query || '%' ORDER BY lastTransactionTimestamp DESC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE phone = :phone LIMIT 1")
    suspend fun getClientByPhone(phone: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE idNumber = :idNumber LIMIT 1")
    suspend fun getClientByIdNumber(idNumber: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE phone = :phone")
    suspend fun deleteClientByPhone(phone: String)
}

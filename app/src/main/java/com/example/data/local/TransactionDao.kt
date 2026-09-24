package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InventoryTransaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM inventory_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<InventoryTransaction>>

    @Query("SELECT * FROM inventory_transactions WHERE beverageId = :beverageId ORDER BY timestamp DESC")
    fun getTransactionsForBeverage(beverageId: Long): Flow<List<InventoryTransaction>>

    @Query("SELECT * FROM inventory_transactions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getTransactionsSince(sinceTimestamp: Long): Flow<List<InventoryTransaction>>

    @Query("SELECT * FROM inventory_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<InventoryTransaction>>

    @Query("SELECT * FROM inventory_transactions WHERE timestamp >= :sinceTimestamp")
    suspend fun getTransactionsSinceSync(sinceTimestamp: Long): List<InventoryTransaction>

    @Query("SELECT SUM(ABS(unitsChanged)) FROM inventory_transactions WHERE type = 'SALE' AND beverageId = :beverageId AND timestamp >= :sinceTimestamp")
    suspend fun getUnitsSoldSince(beverageId: Long, sinceTimestamp: Long): Int?

    @Query("SELECT SUM(totalAmount) FROM inventory_transactions WHERE type = 'SALE' AND timestamp >= :startOfDayTimestamp")
    fun getTodayRevenue(startOfDayTimestamp: Long): Flow<Double?>

    @Query("SELECT SUM(ABS(unitsChanged)) FROM inventory_transactions WHERE type = 'SALE' AND timestamp >= :startOfDayTimestamp")
    fun getTodayUnitsSold(startOfDayTimestamp: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InventoryTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<InventoryTransaction>)

    @Query("DELETE FROM inventory_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

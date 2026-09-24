package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PurchaseOrder
import com.example.data.model.PurchaseOrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseOrderDao {

    @Query("SELECT * FROM purchase_orders ORDER BY createdTimestamp DESC")
    fun getAllOrders(): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchase_orders WHERE status = :status ORDER BY createdTimestamp DESC")
    fun getOrdersByStatus(status: PurchaseOrderStatus): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): PurchaseOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PurchaseOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<PurchaseOrder>)

    @Update
    suspend fun updateOrder(order: PurchaseOrder)

    @Query("UPDATE purchase_orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: PurchaseOrderStatus)

    @Delete
    suspend fun deleteOrder(order: PurchaseOrder)

    @Query("SELECT COUNT(*) FROM purchase_orders")
    suspend fun getCount(): Int
}

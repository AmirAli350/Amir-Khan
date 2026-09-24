package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BeverageItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BeverageDao {

    @Query("SELECT * FROM beverages ORDER BY name ASC")
    fun getAllBeverages(): Flow<List<BeverageItem>>

    @Query("SELECT * FROM beverages WHERE id = :id")
    suspend fun getBeverageById(id: Long): BeverageItem?

    @Query("SELECT * FROM beverages WHERE currentStockUnits <= minStockThresholdUnits ORDER BY currentStockUnits ASC")
    fun getLowStockBeverages(): Flow<List<BeverageItem>>

    @Query("SELECT * FROM beverages WHERE currentStockUnits > 0 AND (:currentTime - lastSoldTimestamp) >= :thresholdMs ORDER BY lastSoldTimestamp ASC")
    fun getOverdueBeverages(currentTime: Long, thresholdMs: Long): Flow<List<BeverageItem>>

    @Query("SELECT * FROM beverages WHERE brand = :brand ORDER BY name ASC")
    fun getBeveragesByBrand(brand: String): Flow<List<BeverageItem>>

    @Query("SELECT * FROM beverages WHERE category = :category ORDER BY name ASC")
    fun getBeveragesByCategory(category: String): Flow<List<BeverageItem>>

    @Query("SELECT * FROM beverages WHERE vendorId = :vendorId ORDER BY name ASC")
    fun getBeveragesByVendor(vendorId: Long): Flow<List<BeverageItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeverage(beverage: BeverageItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beverages: List<BeverageItem>)

    @Update
    suspend fun updateBeverage(beverage: BeverageItem)

    @Query("UPDATE beverages SET currentStockUnits = currentStockUnits + :units, lastRestockedTimestamp = :timestamp WHERE id = :id")
    suspend fun addStock(id: Long, units: Int, timestamp: Long)

    @Query("UPDATE beverages SET currentStockUnits = MAX(0, currentStockUnits - :units), lastSoldTimestamp = :timestamp WHERE id = :id")
    suspend fun deductStock(id: Long, units: Int, timestamp: Long)

    @Delete
    suspend fun deleteBeverage(beverage: BeverageItem)

    @Query("DELETE FROM beverages WHERE id = :id")
    suspend fun deleteBeverageById(id: Long)

    @Query("SELECT COUNT(*) FROM beverages")
    suspend fun getCount(): Int
}

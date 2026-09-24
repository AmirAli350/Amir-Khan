package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Vendor
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {

    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<Vendor>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    suspend fun getVendorById(id: Long): Vendor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: Vendor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vendors: List<Vendor>)

    @Update
    suspend fun updateVendor(vendor: Vendor)

    @Delete
    suspend fun deleteVendor(vendor: Vendor)

    @Query("SELECT COUNT(*) FROM vendors")
    suspend fun getCount(): Int
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Vendor / Beverage Distributor entity
 */
@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // e.g. "Pepsi Beverages North America (PBNA)"
    val contactPerson: String,           // e.g. "Mike Reynolds"
    val phone: String,                   // e.g. "(800) 433-2652"
    val email: String,                   // e.g. "orders@pbna-direct.com"
    val paymentTerms: String = "Net 30", // e.g. "Net 15", "Net 30", "COD"
    val deliveryLeadDays: Int = 3,       // Expected delivery lead time
    val minimumOrderCases: Int = 5,      // Minimum case order
    val notes: String = "Delivery days: Tuesdays & Fridays"
)

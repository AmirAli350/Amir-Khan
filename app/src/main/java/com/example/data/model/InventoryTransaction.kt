package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    SALE,                // Customer purchase (- units)
    RESTOCK,             // Inbound shipment from vendor (+ units)
    RETURN_REFUND,       // Customer returned (+ units)
    SPOILAGE_DAMAGED,    // Expired or damaged can/bottle (- units)
    AUDIT_ADJUSTMENT     // Physical cycle count adjustment
}

/**
 * Historical inventory transaction log
 */
@Entity(tableName = "inventory_transactions")
data class InventoryTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val beverageId: Long,
    val beverageName: String,
    val type: TransactionType,
    val unitsChanged: Int,         // Positive for increase (restock), negative for decrease (sale)
    val unitPrice: Double,         // Selling price if sale, buying cost if restock
    val totalAmount: Double,       // unitsChanged * unitPrice
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

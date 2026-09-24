package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PurchaseOrderStatus {
    DRAFT,
    SUBMITTED,
    OVERDUE,
    RECEIVED,
    CANCELLED
}

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val vendorId: Long,
    val vendorName: String,
    val totalCases: Int,
    val totalEstimatedCost: Double,
    val status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val expectedDeliveryTimestamp: Long = System.currentTimeMillis() + (3L * 24L * 60L * 60L * 1000L),
    val itemsSummary: String = "",       // e.g. "Pepsi 12oz (4 cs), Mtn Dew (2 cs)"
    val notes: String = ""
) {
    /**
     * Check if this purchase order is overdue by 10 or more days past expected delivery date
     */
    fun isOverdueBy10Days(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (status == PurchaseOrderStatus.RECEIVED || status == PurchaseOrderStatus.CANCELLED) return false
        val tenDaysMs = 10L * 24L * 60L * 60L * 1000L
        return (currentTime - expectedDeliveryTimestamp) >= tenDaysMs
    }
}

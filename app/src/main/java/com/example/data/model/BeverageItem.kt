package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Beverage item entity for retail soda & Pepsi stock management.
 * Tracks units, case configurations, wholesale & retail pricing, and vendor links.
 */
@Entity(tableName = "beverages")
data class BeverageItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,                      // e.g. "PEP-12OZ-CAN", "MTN-20OZ-BTL"
    val name: String,                     // e.g. "Pepsi Regular"
    val brand: String,                    // e.g. "Pepsi", "Mountain Dew", "7Up"
    val category: String,                 // e.g. "Cola", "Citrus", "Root Beer", "Flavored", "Energy & Sports", "Sparkling Water"
    val packageType: String,              // e.g. "12 oz Can", "20 oz Bottle", "2-Liter Bottle", "12-Pack Case"
    val unitsPerCase: Int = 24,           // Standard wholesale case size
    val currentStockUnits: Int,           // Real-time on-hand units in store
    val minStockThresholdUnits: Int = 12, // Low stock trigger threshold
    val targetStockUnits: Int = 48,       // Desired par inventory level
    val buyingPrice: Double,              // Store unit buying cost (e.g. $0.45 per can)
    val wholesalePrice: Double,           // Wholesale cost per full case (e.g. $10.80 per case of 24)
    val sellingPrice: Double,             // Retail selling price per unit to customers (e.g. $1.29)
    val vendorId: Long = 1,               // Linked vendor ID
    val lastRestockedTimestamp: Long = System.currentTimeMillis(),
    val lastSoldTimestamp: Long = System.currentTimeMillis(),
    val leadTimeDays: Int = 3,            // Vendor delivery lead time in days
    val notes: String = ""
) {
    /** Number of full cases on hand */
    val casesOnHand: Int
        get() = if (unitsPerCase > 0) currentStockUnits / unitsPerCase else 0

    /** Loose leftover units outside of full cases */
    val looseUnitsOnHand: Int
        get() = if (unitsPerCase > 0) currentStockUnits % unitsPerCase else currentStockUnits

    /** Gross profit margin per unit ($) */
    val unitProfit: Double
        get() = sellingPrice - buyingPrice

    /** Gross profit margin percentage (%) */
    val marginPercentage: Double
        get() = if (sellingPrice > 0) ((sellingPrice - buyingPrice) / sellingPrice) * 100.0 else 0.0

    /** Markup percentage over cost (%) */
    val markupPercentage: Double
        get() = if (buyingPrice > 0) ((sellingPrice - buyingPrice) / buyingPrice) * 100.0 else 0.0

    /** Estimated gross profit for a full wholesale case sold at retail ($) */
    val fullCaseProfit: Double
        get() = (sellingPrice * unitsPerCase) - wholesalePrice

    /** Current stock valuation at buying cost */
    val totalCostValue: Double
        get() = currentStockUnits * buyingPrice

    /** Current stock valuation at retail selling price */
    val totalRetailValue: Double
        get() = currentStockUnits * sellingPrice

    /** Potential gross profit of inventory currently on hand */
    val totalPotentialProfit: Double
        get() = totalRetailValue - totalCostValue

    /** Check if stock is at or below minimum threshold */
    val isLowStock: Boolean
        get() = currentStockUnits <= minStockThresholdUnits

    /** Check if stock is completely out of stock */
    val isOutOfStock: Boolean
        get() = currentStockUnits <= 0

    /**
     * Check if stock has been overdue/stagnant for 10 or more days
     * (i.e. sitting in inventory with no sales activity for 10+ days).
     */
    fun isOverdueForDays(currentTimestamp: Long = System.currentTimeMillis(), days: Int = 10): Boolean {
        if (currentStockUnits <= 0) return false
        val tenDaysMs = days * 24L * 60L * 60L * 1000L
        val timeSinceLastSold = currentTimestamp - lastSoldTimestamp
        return timeSinceLastSold >= tenDaysMs
    }

    /** Days elapsed since last sale */
    fun daysSinceLastSale(currentTimestamp: Long = System.currentTimeMillis()): Int {
        val diffMs = currentTimestamp - lastSoldTimestamp
        return (diffMs / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
    }

    /** Days elapsed since last restock */
    fun daysSinceLastRestock(currentTimestamp: Long = System.currentTimeMillis()): Int {
        val diffMs = currentTimestamp - lastRestockedTimestamp
        return (diffMs / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
    }
}

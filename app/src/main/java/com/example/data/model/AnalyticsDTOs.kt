package com.example.data.model

/**
 * Calculated analytics and turnover metrics for a single beverage item
 */
data class BeverageAnalytics(
    val beverage: BeverageItem,
    val unitsSoldPeriod: Int,           // e.g. units sold in past 14/30 days
    val averageDailySales: Double,      // ADS (units/day)
    val daysOfInventoryRemaining: Double, // Current units / ADS
    val turnoverRate: Double,           // Annualized turnover rate (e.g. 8.5x)
    val velocityCategory: VelocityTier, // FAST, MEDIUM, SLOW, DEAD
    val isReorderRecommended: Boolean,
    val suggestedOrderCases: Int,       // Cases to order to reach target level
    val estimatedOrderCost: Double,     // suggestedOrderCases * wholesalePrice
    val isOverdueTenDays: Boolean,      // Stagnant with no sales for >= 10 days
    val daysSinceLastMovement: Int
)

enum class VelocityTier(val label: String) {
    FAST("Fast Mover (A)"),
    MEDIUM("Steady (B)"),
    SLOW("Slow Mover (C)"),
    STAGNANT("Overdue / Dead Stock")
}

/**
 * High-level store inventory valuation and metrics
 */
data class StoreInventorySummary(
    val totalBeverageCount: Int = 0,
    val totalUnitsOnHand: Int = 0,
    val totalCasesOnHand: Int = 0,
    val totalCostValuation: Double = 0.0,      // Total inventory valued at buying price
    val totalWholesaleValuation: Double = 0.0, // Total inventory valued at case wholesale price
    val totalRetailValuation: Double = 0.0,    // Total inventory valued at retail selling price
    val totalPotentialProfit: Double = 0.0,    // Potential gross profit in stock
    val averageProfitMarginPercent: Double = 0.0,
    val lowStockCount: Int = 0,
    val overdueStockCount: Int = 0,            // Items overdue/stagnant >= 10 days
    val totalSalesTodayUnits: Int = 0,
    val totalRevenueToday: Double = 0.0
)

/**
 * Overdue stock alert item
 */
data class OverdueStockAlert(
    val beverage: BeverageItem,
    val daysInactive: Int,
    val tiedUpCapital: Double,
    val suggestedAction: String
)

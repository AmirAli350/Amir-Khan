package com.example.domain

import com.example.data.model.BeverageAnalytics
import com.example.data.model.BeverageItem
import com.example.data.model.InventoryTransaction
import com.example.data.model.OverdueStockAlert
import com.example.data.model.StoreInventorySummary
import com.example.data.model.TransactionType
import com.example.data.model.VelocityTier
import kotlin.math.ceil
import kotlin.math.roundToInt

object InventoryCalculator {

    private const val DAYS_IN_ANALYSIS_WINDOW = 14
    private const val OVERDUE_DAYS_THRESHOLD = 10

    /**
     * Compute comprehensive analytics for a beverage item based on historical transactions
     */
    fun computeBeverageAnalytics(
        beverage: BeverageItem,
        recentTransactions: List<InventoryTransaction>,
        currentTimestamp: Long = System.currentTimeMillis()
    ): BeverageAnalytics {
        val windowMs = DAYS_IN_ANALYSIS_WINDOW * 24L * 60L * 60L * 1000L
        val windowStart = currentTimestamp - windowMs

        // Filter sales for this specific beverage within the analysis window
        val salesInWindow = recentTransactions.filter {
            it.beverageId == beverage.id &&
            it.type == TransactionType.SALE &&
            it.timestamp >= windowStart
        }

        val totalUnitsSoldInWindow = salesInWindow.sumOf { kotlin.math.abs(it.unitsChanged) }

        // Average Daily Sales (ADS)
        val averageDailySales = if (DAYS_IN_ANALYSIS_WINDOW > 0) {
            totalUnitsSoldInWindow.toDouble() / DAYS_IN_ANALYSIS_WINDOW.toDouble()
        } else 0.0

        // Days of Inventory Remaining (DOIR)
        val daysRemaining = if (averageDailySales > 0.01) {
            beverage.currentStockUnits / averageDailySales
        } else {
            999.0 // Infinite/stagnant
        }

        // Annualized Turnover Rate (ITR) = (Units sold annualized) / (Current stock units, minimum 1)
        val annualizedSalesUnits = averageDailySales * 365.0
        val effectiveStock = beverage.currentStockUnits.coerceAtLeast(1)
        val turnoverRate = (annualizedSalesUnits / effectiveStock.toDouble())

        // Days since last movement
        val daysSinceLastSale = beverage.daysSinceLastSale(currentTimestamp)
        val isOverdueTenDays = beverage.isOverdueForDays(currentTimestamp, OVERDUE_DAYS_THRESHOLD)

        // Classify velocity tier
        val velocity = when {
            isOverdueTenDays -> VelocityTier.STAGNANT
            turnoverRate >= 6.0 || averageDailySales >= 4.0 -> VelocityTier.FAST
            turnoverRate >= 2.0 -> VelocityTier.MEDIUM
            else -> VelocityTier.SLOW
        }

        // Automated Reorder Calculation:
        // Reorder point = (ADS * leadTimeDays) + safety stock (minStockThresholdUnits)
        val reorderPointUnits = (averageDailySales * beverage.leadTimeDays).roundToInt() + beverage.minStockThresholdUnits
        val isReorderRecommended = beverage.currentStockUnits <= beverage.minStockThresholdUnits ||
                beverage.currentStockUnits <= reorderPointUnits

        // Suggested order quantity in full cases
        val deficitUnits = (beverage.targetStockUnits - beverage.currentStockUnits).coerceAtLeast(0)
        val suggestedOrderCases = if (isReorderRecommended && deficitUnits > 0) {
            ceil(deficitUnits.toDouble() / beverage.unitsPerCase.coerceAtLeast(1).toDouble()).toInt().coerceAtLeast(1)
        } else {
            0
        }
        val estimatedOrderCost = suggestedOrderCases * beverage.wholesalePrice

        return BeverageAnalytics(
            beverage = beverage,
            unitsSoldPeriod = totalUnitsSoldInWindow,
            averageDailySales = (averageDailySales * 10.0).roundToInt() / 10.0,
            daysOfInventoryRemaining = (daysRemaining * 10.0).roundToInt() / 10.0,
            turnoverRate = (turnoverRate * 10.0).roundToInt() / 10.0,
            velocityCategory = velocity,
            isReorderRecommended = isReorderRecommended,
            suggestedOrderCases = suggestedOrderCases,
            estimatedOrderCost = estimatedOrderCost,
            isOverdueTenDays = isOverdueTenDays,
            daysSinceLastMovement = daysSinceLastSale
        )
    }

    /**
     * Calculate aggregate store inventory summary (valuation, retail value, margins, alerts)
     */
    fun computeStoreSummary(
        beverages: List<BeverageItem>,
        recentTransactions: List<InventoryTransaction>,
        currentTimestamp: Long = System.currentTimeMillis()
    ): StoreInventorySummary {
        val totalUnits = beverages.sumOf { it.currentStockUnits }
        val totalCases = beverages.sumOf { it.casesOnHand }
        val totalCostValuation = beverages.sumOf { it.totalCostValue }
        val totalRetailValuation = beverages.sumOf { it.totalRetailValue }
        val totalWholesaleValuation = beverages.sumOf {
            (it.currentStockUnits.toDouble() / it.unitsPerCase.coerceAtLeast(1).toDouble()) * it.wholesalePrice
        }
        val totalPotentialProfit = totalRetailValuation - totalCostValuation
        val avgMargin = if (totalRetailValuation > 0) {
            (totalPotentialProfit / totalRetailValuation) * 100.0
        } else 0.0

        val lowStockCount = beverages.count { it.isLowStock }
        val overdueStockCount = beverages.count { it.isOverdueForDays(currentTimestamp, OVERDUE_DAYS_THRESHOLD) }

        // Today's sales from midnight
        val startOfToday = currentTimestamp - (currentTimestamp % (24L * 60L * 60L * 1000L))
        val todaySales = recentTransactions.filter {
            it.type == TransactionType.SALE && it.timestamp >= startOfToday
        }
        val totalSalesTodayUnits = todaySales.sumOf { kotlin.math.abs(it.unitsChanged) }
        val totalRevenueToday = todaySales.sumOf { it.totalAmount }

        return StoreInventorySummary(
            totalBeverageCount = beverages.size,
            totalUnitsOnHand = totalUnits,
            totalCasesOnHand = totalCases,
            totalCostValuation = totalCostValuation,
            totalWholesaleValuation = totalWholesaleValuation,
            totalRetailValuation = totalRetailValuation,
            totalPotentialProfit = totalPotentialProfit,
            averageProfitMarginPercent = avgMargin,
            lowStockCount = lowStockCount,
            overdueStockCount = overdueStockCount,
            totalSalesTodayUnits = totalSalesTodayUnits,
            totalRevenueToday = totalRevenueToday
        )
    }

    /**
     * Generate list of overdue alerts with actionable recommendations
     */
    fun generateOverdueAlerts(
        beverages: List<BeverageItem>,
        currentTimestamp: Long = System.currentTimeMillis()
    ): List<OverdueStockAlert> {
        return beverages
            .filter { it.isOverdueForDays(currentTimestamp, OVERDUE_DAYS_THRESHOLD) }
            .sortedByDescending { it.daysSinceLastSale(currentTimestamp) }
            .map { item ->
                val days = item.daysSinceLastSale(currentTimestamp)
                val tiedUp = item.totalCostValue
                val action = when {
                    days >= 20 -> "Urgent: Place on discount clearance (-25%) or bundle with snacks to free up shelf space."
                    days >= 14 -> "Notice: Reposition stock to eye-level front counter cooler or run a combo promo."
                    else -> "Warning: 10+ days inactive. Check expiry dates and verify shelf placement."
                }
                OverdueStockAlert(
                    beverage = item,
                    daysInactive = days,
                    tiedUpCapital = tiedUp,
                    suggestedAction = action
                )
            }
    }
}

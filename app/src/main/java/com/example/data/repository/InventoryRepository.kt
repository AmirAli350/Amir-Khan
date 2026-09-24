package com.example.data.repository

import com.example.data.local.BeverageDao
import com.example.data.local.PurchaseOrderDao
import com.example.data.local.TransactionDao
import com.example.data.local.VendorDao
import com.example.data.model.BeverageItem
import com.example.data.model.InventoryTransaction
import com.example.data.model.PurchaseOrder
import com.example.data.model.PurchaseOrderStatus
import com.example.data.model.TransactionType
import com.example.data.model.Vendor
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryRepository(
    private val beverageDao: BeverageDao,
    private val vendorDao: VendorDao,
    private val transactionDao: TransactionDao,
    private val purchaseOrderDao: PurchaseOrderDao
) {
    val allBeverages: Flow<List<BeverageItem>> = beverageDao.getAllBeverages()
    val lowStockBeverages: Flow<List<BeverageItem>> = beverageDao.getLowStockBeverages()
    val allVendors: Flow<List<Vendor>> = vendorDao.getAllVendors()
    val allTransactions: Flow<List<InventoryTransaction>> = transactionDao.getAllTransactions()
    val allOrders: Flow<List<PurchaseOrder>> = purchaseOrderDao.getAllOrders()

    suspend fun getBeverageById(id: Long): BeverageItem? = beverageDao.getBeverageById(id)

    suspend fun insertBeverage(beverage: BeverageItem): Long = beverageDao.insertBeverage(beverage)

    suspend fun updateBeverage(beverage: BeverageItem) = beverageDao.updateBeverage(beverage)

    suspend fun deleteBeverage(beverage: BeverageItem) = beverageDao.deleteBeverage(beverage)

    suspend fun deleteBeverageById(id: Long) = beverageDao.deleteBeverageById(id)

    /**
     * Record a sale: deduct stock, update lastSoldTimestamp, log transaction
     */
    suspend fun recordSale(
        beverageId: Long,
        units: Int,
        customPrice: Double? = null,
        notes: String = ""
    ) {
        val beverage = beverageDao.getBeverageById(beverageId) ?: return
        val now = System.currentTimeMillis()
        val price = customPrice ?: beverage.sellingPrice
        val total = units * price

        beverageDao.deductStock(beverageId, units, now)

        val tx = InventoryTransaction(
            beverageId = beverageId,
            beverageName = beverage.name,
            type = TransactionType.SALE,
            unitsChanged = -units,
            unitPrice = price,
            totalAmount = total,
            timestamp = now,
            notes = notes.ifEmpty { "Counter sale ($units units)" }
        )
        transactionDao.insertTransaction(tx)
    }

    /**
     * Record a restock receipt: add stock, update lastRestockedTimestamp, log transaction
     */
    suspend fun recordRestock(
        beverageId: Long,
        units: Int,
        customUnitCost: Double? = null,
        notes: String = ""
    ) {
        val beverage = beverageDao.getBeverageById(beverageId) ?: return
        val now = System.currentTimeMillis()
        val cost = customUnitCost ?: beverage.buyingPrice
        val total = units * cost

        beverageDao.addStock(beverageId, units, now)

        val tx = InventoryTransaction(
            beverageId = beverageId,
            beverageName = beverage.name,
            type = TransactionType.RESTOCK,
            unitsChanged = units,
            unitPrice = cost,
            totalAmount = total,
            timestamp = now,
            notes = notes.ifEmpty { "Received delivery (+$units units)" }
        )
        transactionDao.insertTransaction(tx)
    }

    /**
     * Adjust physical inventory count during cycle count / store audit
     */
    suspend fun recordAdjustment(
        beverageId: Long,
        newStockUnits: Int,
        reason: String
    ) {
        val beverage = beverageDao.getBeverageById(beverageId) ?: return
        val diff = newStockUnits - beverage.currentStockUnits
        val now = System.currentTimeMillis()

        val updated = beverage.copy(
            currentStockUnits = newStockUnits.coerceAtLeast(0),
            lastRestockedTimestamp = if (diff > 0) now else beverage.lastRestockedTimestamp
        )
        beverageDao.updateBeverage(updated)

        val tx = InventoryTransaction(
            beverageId = beverageId,
            beverageName = beverage.name,
            type = TransactionType.AUDIT_ADJUSTMENT,
            unitsChanged = diff,
            unitPrice = beverage.buyingPrice,
            totalAmount = diff * beverage.buyingPrice,
            timestamp = now,
            notes = "Audit adjustment: $reason (old: ${beverage.currentStockUnits}, new: $newStockUnits)"
        )
        transactionDao.insertTransaction(tx)
    }

    /**
     * Mark an item as damaged/spoiled/expired
     */
    suspend fun recordDamagedLoss(
        beverageId: Long,
        units: Int,
        notes: String
    ) {
        val beverage = beverageDao.getBeverageById(beverageId) ?: return
        val now = System.currentTimeMillis()

        beverageDao.deductStock(beverageId, units, now)

        val tx = InventoryTransaction(
            beverageId = beverageId,
            beverageName = beverage.name,
            type = TransactionType.SPOILAGE_DAMAGED,
            unitsChanged = -units,
            unitPrice = beverage.buyingPrice,
            totalAmount = -(units * beverage.buyingPrice),
            timestamp = now,
            notes = "Damaged/Spoiled: $notes"
        )
        transactionDao.insertTransaction(tx)
    }

    // Vendor Operations
    suspend fun insertVendor(vendor: Vendor): Long = vendorDao.insertVendor(vendor)
    suspend fun updateVendor(vendor: Vendor) = vendorDao.updateVendor(vendor)
    suspend fun deleteVendor(vendor: Vendor) = vendorDao.deleteVendor(vendor)
    suspend fun getVendorById(id: Long): Vendor? = vendorDao.getVendorById(id)

    // Purchase Order & Automated Reorder Operations
    suspend fun createPurchaseOrder(
        vendorId: Long,
        vendorName: String,
        totalCases: Int,
        totalEstimatedCost: Double,
        itemsSummary: String,
        leadTimeDays: Int = 3,
        notes: String = ""
    ): Long {
        val dateStr = SimpleDateFormat("yyMMdd-HHmm", Locale.US).format(Date())
        val order = PurchaseOrder(
            orderNumber = "PO-$dateStr",
            vendorId = vendorId,
            vendorName = vendorName,
            totalCases = totalCases,
            totalEstimatedCost = totalEstimatedCost,
            status = PurchaseOrderStatus.SUBMITTED,
            createdTimestamp = System.currentTimeMillis(),
            expectedDeliveryTimestamp = System.currentTimeMillis() + (leadTimeDays * 24L * 60L * 60L * 1000L),
            itemsSummary = itemsSummary,
            notes = notes
        )
        return purchaseOrderDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(id: Long, status: PurchaseOrderStatus) {
        purchaseOrderDao.updateOrderStatus(id, status)
    }

    suspend fun deleteOrder(order: PurchaseOrder) = purchaseOrderDao.deleteOrder(order)
}

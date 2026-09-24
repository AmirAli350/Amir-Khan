package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BeverageItem
import com.example.data.model.InventoryTransaction
import com.example.data.model.PurchaseOrder
import com.example.data.model.PurchaseOrderStatus
import com.example.data.model.TransactionType
import com.example.data.model.Vendor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BeverageItem::class,
        Vendor::class,
        InventoryTransaction::class,
        PurchaseOrder::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun beverageDao(): BeverageDao
    abstract fun vendorDao(): VendorDao
    abstract fun transactionDao(): TransactionDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soda_inventory_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60L * 60L * 1000L

            // 1. Initial Vendors
            val pbna = Vendor(
                id = 1,
                name = "Pepsi Beverages North America (PBNA)",
                contactPerson = "Dave Thompson",
                phone = "(800) 433-2652",
                email = "orders@pbna-direct.com",
                paymentTerms = "Net 30",
                deliveryLeadDays = 3,
                minimumOrderCases = 10,
                notes = "Primary distributor for Pepsi, Dew, Starry, Bubly & Gatorade. Delivery: Tue & Fri."
            )
            val metroWholesale = Vendor(
                id = 2,
                name = "Metro Beverage Wholesalers",
                contactPerson = "Sarah Lin",
                phone = "(555) 789-2341",
                email = "sales@metrobev.com",
                paymentTerms = "Net 15",
                deliveryLeadDays = 2,
                minimumOrderCases = 5,
                notes = "Secondary distributor for Dr Pepper, 7Up, specialty sodas. Next-day delivery available."
            )
            val regionalBottlers = Vendor(
                id = 3,
                name = "Regional Bottlers Co-Op",
                contactPerson = "Carlos Rivera",
                phone = "(555) 345-6789",
                email = "orders@regionalbottlers.com",
                paymentTerms = "COD",
                deliveryLeadDays = 4,
                minimumOrderCases = 8,
                notes = "Bottles and 2-liter bulk stock supplier."
            )
            database.vendorDao().insertAll(listOf(pbna, metroWholesale, regionalBottlers))

            // 2. Initial Beverages with Wholesale, Buying, and Selling Prices
            val initialBeverages = listOf(
                BeverageItem(
                    id = 1,
                    sku = "PEP-12OZ-CAN",
                    name = "Pepsi Regular",
                    brand = "Pepsi",
                    category = "Cola",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 68,
                    minStockThresholdUnits = 24,
                    targetStockUnits = 120,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (2 * dayMs),
                    lastSoldTimestamp = now - (2 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 2,
                    sku = "DPEP-12OZ-CAN",
                    name = "Diet Pepsi",
                    brand = "Pepsi",
                    category = "Cola",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 42,
                    minStockThresholdUnits = 20,
                    targetStockUnits = 72,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (3 * dayMs),
                    lastSoldTimestamp = now - (5 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 3,
                    sku = "PEPZERO-12OZ-CAN",
                    name = "Pepsi Zero Sugar",
                    brand = "Pepsi",
                    category = "Cola",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 14, // LOW STOCK TRIGGER
                    minStockThresholdUnits = 20,
                    targetStockUnits = 72,
                    buyingPrice = 0.48,
                    wholesalePrice = 11.50,
                    sellingPrice = 1.29,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (6 * dayMs),
                    lastSoldTimestamp = now - (1 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 4,
                    sku = "PEP-CHERRY-20OZ",
                    name = "Pepsi Wild Cherry",
                    brand = "Pepsi",
                    category = "Cola",
                    packageType = "20 oz Bottle (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 28,
                    minStockThresholdUnits = 16,
                    targetStockUnits = 48,
                    buyingPrice = 0.85,
                    wholesalePrice = 20.40,
                    sellingPrice = 2.19,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (4 * dayMs),
                    lastSoldTimestamp = now - (8 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 5,
                    sku = "PEP-2L-BTL",
                    name = "Pepsi Regular 2-Liter",
                    brand = "Pepsi",
                    category = "Cola",
                    packageType = "2-Liter Bottle (8/Case)",
                    unitsPerCase = 8,
                    currentStockUnits = 8, // LOW STOCK TRIGGER
                    minStockThresholdUnits = 12,
                    targetStockUnits = 32,
                    buyingPrice = 1.20,
                    wholesalePrice = 9.60,
                    sellingPrice = 2.69,
                    vendorId = 3,
                    lastRestockedTimestamp = now - (7 * dayMs),
                    lastSoldTimestamp = now - (4 * 3600 * 1000L),
                    leadTimeDays = 4
                ),
                BeverageItem(
                    id = 6,
                    sku = "MTN-12OZ-CAN",
                    name = "Mountain Dew",
                    brand = "Mountain Dew",
                    category = "Citrus Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 52,
                    minStockThresholdUnits = 24,
                    targetStockUnits = 96,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (2 * dayMs),
                    lastSoldTimestamp = now - (1 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 7,
                    sku = "MTN-BAJA-12OZ",
                    name = "Mountain Dew Baja Blast",
                    brand = "Mountain Dew",
                    category = "Citrus Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 9, // LOW STOCK TRIGGER
                    minStockThresholdUnits = 18,
                    targetStockUnits = 72,
                    buyingPrice = 0.50,
                    wholesalePrice = 12.00,
                    sellingPrice = 1.39,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (5 * dayMs),
                    lastSoldTimestamp = now - (3 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 8,
                    sku = "STRY-12OZ-CAN",
                    name = "Starry Lemon Lime",
                    brand = "Starry",
                    category = "Citrus Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 36,
                    minStockThresholdUnits = 18,
                    targetStockUnits = 48,
                    buyingPrice = 0.42,
                    wholesalePrice = 10.08,
                    sellingPrice = 1.19,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (4 * dayMs),
                    lastSoldTimestamp = now - (12 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 9,
                    sku = "7UP-12OZ-CAN",
                    name = "7UP Lemon Lime",
                    brand = "7Up",
                    category = "Citrus Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 24,
                    minStockThresholdUnits = 16,
                    targetStockUnits = 48,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 2,
                    lastRestockedTimestamp = now - (5 * dayMs),
                    lastSoldTimestamp = now - (18 * 3600 * 1000L),
                    leadTimeDays = 2
                ),
                BeverageItem(
                    id = 10,
                    sku = "MUG-12OZ-CAN",
                    name = "Mug Root Beer",
                    brand = "Mug",
                    category = "Root Beer",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 30,
                    minStockThresholdUnits = 16,
                    targetStockUnits = 48,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (6 * dayMs),
                    lastSoldTimestamp = now - (14 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 11,
                    sku = "CRUSH-ORG-12OZ",
                    name = "Crush Orange Soda",
                    brand = "Crush",
                    category = "Flavored Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 22,
                    minStockThresholdUnits = 14,
                    targetStockUnits = 48,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (5 * dayMs),
                    lastSoldTimestamp = now - (16 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 12,
                    sku = "DRP-12OZ-CAN",
                    name = "Dr Pepper Original",
                    brand = "Dr Pepper",
                    category = "Specialty Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 40,
                    minStockThresholdUnits = 20,
                    targetStockUnits = 72,
                    buyingPrice = 0.48,
                    wholesalePrice = 11.50,
                    sellingPrice = 1.29,
                    vendorId = 2,
                    lastRestockedTimestamp = now - (3 * dayMs),
                    lastSoldTimestamp = now - (3 * 3600 * 1000L),
                    leadTimeDays = 2
                ),
                BeverageItem(
                    id = 13,
                    sku = "GAT-BLUE-20OZ",
                    name = "Gatorade Cool Blue",
                    brand = "Gatorade",
                    category = "Energy & Sports",
                    packageType = "20 oz Bottle (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 34,
                    minStockThresholdUnits = 16,
                    targetStockUnits = 48,
                    buyingPrice = 0.90,
                    wholesalePrice = 21.60,
                    sellingPrice = 2.29,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (4 * dayMs),
                    lastSoldTimestamp = now - (6 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 14,
                    sku = "BUB-LIME-12OZ",
                    name = "Bubly Sparkling Lime",
                    brand = "Bubly",
                    category = "Sparkling Water",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 26,
                    minStockThresholdUnits = 16,
                    targetStockUnits = 48,
                    buyingPrice = 0.40,
                    wholesalePrice = 9.60,
                    sellingPrice = 1.15,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (5 * dayMs),
                    lastSoldTimestamp = now - (10 * 3600 * 1000L),
                    leadTimeDays = 3
                ),
                BeverageItem(
                    id = 15,
                    sku = "AQF-169OZ-BTL",
                    name = "Aquafina Purified Water",
                    brand = "Aquafina",
                    category = "Tea & Water",
                    packageType = "16.9 oz Bottle (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 55,
                    minStockThresholdUnits = 24,
                    targetStockUnits = 96,
                    buyingPrice = 0.30,
                    wholesalePrice = 7.20,
                    sellingPrice = 1.00,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (2 * dayMs),
                    lastSoldTimestamp = now - (1 * 3600 * 1000L),
                    leadTimeDays = 3
                ),

                // OVERDUE FOR 10+ DAYS ITEMS (Stagnant / dead stock alerts requested by user!)
                BeverageItem(
                    id = 16,
                    sku = "PEP-NITRO-VAN",
                    name = "Pepsi Nitro Vanilla Draft Cola",
                    brand = "Pepsi",
                    category = "Cola",
                    packageType = "13.65 oz Can (12/Case)",
                    unitsPerCase = 12,
                    currentStockUnits = 18,
                    minStockThresholdUnits = 10,
                    targetStockUnits = 24,
                    buyingPrice = 1.10,
                    wholesalePrice = 13.20,
                    sellingPrice = 2.49,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (22 * dayMs),
                    lastSoldTimestamp = now - (14 * dayMs), // OVERDUE: 14 days without sale!
                    leadTimeDays = 3,
                    notes = "Slow demand in winter. Consider clearance promo."
                ),
                BeverageItem(
                    id = 17,
                    sku = "MIR-ORG-12OZ",
                    name = "Mirinda Orange Soda",
                    brand = "Mirinda",
                    category = "Flavored Soda",
                    packageType = "12 oz Can (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 16,
                    minStockThresholdUnits = 8,
                    targetStockUnits = 24,
                    buyingPrice = 0.45,
                    wholesalePrice = 10.80,
                    sellingPrice = 1.25,
                    vendorId = 2,
                    lastRestockedTimestamp = now - (18 * dayMs),
                    lastSoldTimestamp = now - (12 * dayMs), // OVERDUE: 12 days without sale!
                    leadTimeDays = 2,
                    notes = "Overdue stock. Reposition near snack aisle."
                ),
                BeverageItem(
                    id = 18,
                    sku = "MUG-CREAM-20OZ",
                    name = "Mug Cream Soda",
                    brand = "Mug",
                    category = "Specialty Soda",
                    packageType = "20 oz Bottle (24/Case)",
                    unitsPerCase = 24,
                    currentStockUnits = 12,
                    minStockThresholdUnits = 6,
                    targetStockUnits = 24,
                    buyingPrice = 0.85,
                    wholesalePrice = 20.40,
                    sellingPrice = 2.19,
                    vendorId = 1,
                    lastRestockedTimestamp = now - (16 * dayMs),
                    lastSoldTimestamp = now - (11 * dayMs), // OVERDUE: 11 days without sale!
                    leadTimeDays = 3
                )
            )
            database.beverageDao().insertAll(initialBeverages)

            // 3. Initial Historical Transactions (realistic sales velocity for trends calculation)
            val transactions = mutableListOf<InventoryTransaction>()
            // Recent sales today
            transactions.add(
                InventoryTransaction(
                    beverageId = 1,
                    beverageName = "Pepsi Regular",
                    type = TransactionType.SALE,
                    unitsChanged = -6,
                    unitPrice = 1.25,
                    totalAmount = 7.50,
                    timestamp = now - (2 * 3600 * 1000L),
                    notes = "Afternoon counter sales"
                )
            )
            transactions.add(
                InventoryTransaction(
                    beverageId = 6,
                    beverageName = "Mountain Dew",
                    type = TransactionType.SALE,
                    unitsChanged = -4,
                    unitPrice = 1.25,
                    totalAmount = 5.00,
                    timestamp = now - (1 * 3600 * 1000L),
                    notes = "Cold grab-and-go"
                )
            )
            transactions.add(
                InventoryTransaction(
                    beverageId = 3,
                    beverageName = "Pepsi Zero Sugar",
                    type = TransactionType.SALE,
                    unitsChanged = -8,
                    unitPrice = 1.29,
                    totalAmount = 10.32,
                    timestamp = now - (4 * 3600 * 1000L),
                    notes = "Lunch crowd rush"
                )
            )
            // Sales over past 7 days to simulate turnover velocity
            for (day in 1..7) {
                val tStamp = now - (day * dayMs) + (4 * 3600 * 1000L)
                transactions.add(
                    InventoryTransaction(
                        beverageId = 1,
                        beverageName = "Pepsi Regular",
                        type = TransactionType.SALE,
                        unitsChanged = -(10 + (day % 4)),
                        unitPrice = 1.25,
                        totalAmount = (10 + (day % 4)) * 1.25,
                        timestamp = tStamp,
                        notes = "Daily retail counter log"
                    )
                )
                transactions.add(
                    InventoryTransaction(
                        beverageId = 6,
                        beverageName = "Mountain Dew",
                        type = TransactionType.SALE,
                        unitsChanged = -(8 + (day % 3)),
                        unitPrice = 1.25,
                        totalAmount = (8 + (day % 3)) * 1.25,
                        timestamp = tStamp + 1800000L,
                        notes = "Daily retail counter log"
                    )
                )
                transactions.add(
                    InventoryTransaction(
                        beverageId = 15,
                        beverageName = "Aquafina Purified Water",
                        type = TransactionType.SALE,
                        unitsChanged = -(12 + (day % 5)),
                        unitPrice = 1.00,
                        totalAmount = (12 + (day % 5)) * 1.00,
                        timestamp = tStamp + 3600000L,
                        notes = "Bottled water counter log"
                    )
                )
            }
            // Recent Restock from PBNA
            transactions.add(
                InventoryTransaction(
                    beverageId = 1,
                    beverageName = "Pepsi Regular",
                    type = TransactionType.RESTOCK,
                    unitsChanged = 48, // 2 cases
                    unitPrice = 0.45,
                    totalAmount = 21.60,
                    timestamp = now - (2 * dayMs),
                    notes = "PBNA delivery #PO-8821 (2 cases)"
                )
            )
            database.transactionDao().insertAll(transactions)

            // 4. Initial Sample Purchase Order & Reorder Notification
            val po1 = PurchaseOrder(
                id = 1,
                orderNumber = "PO-8821",
                vendorId = 1,
                vendorName = "Pepsi Beverages North America (PBNA)",
                totalCases = 6,
                totalEstimatedCost = 67.20,
                status = PurchaseOrderStatus.RECEIVED,
                createdTimestamp = now - (5 * dayMs),
                expectedDeliveryTimestamp = now - (2 * dayMs),
                itemsSummary = "Pepsi Regular 12oz (2 cs), Mtn Dew 12oz (2 cs), Aquafina (2 cs)",
                notes = "Received and stocked."
            )
            // An active reorder for low stock items
            val po2 = PurchaseOrder(
                id = 2,
                orderNumber = "PO-8940",
                vendorId = 1,
                vendorName = "Pepsi Beverages North America (PBNA)",
                totalCases = 5,
                totalEstimatedCost = 57.00,
                status = PurchaseOrderStatus.SUBMITTED,
                createdTimestamp = now - (1 * dayMs),
                expectedDeliveryTimestamp = now + (2 * dayMs),
                itemsSummary = "Pepsi Zero Sugar (3 cs), Mtn Dew Baja Blast (2 cs)",
                notes = "Automated reorder triggered by low stock thresholds."
            )
            database.purchaseOrderDao().insertAll(listOf(po1, po2))
        }
    }
}

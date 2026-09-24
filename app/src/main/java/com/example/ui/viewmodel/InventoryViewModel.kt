package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BeverageAnalytics
import com.example.data.model.BeverageItem
import com.example.data.model.InventoryTransaction
import com.example.data.model.OverdueStockAlert
import com.example.data.model.PurchaseOrder
import com.example.data.model.PurchaseOrderStatus
import com.example.data.model.StoreInventorySummary
import com.example.data.model.Vendor
import com.example.data.repository.InventoryRepository
import com.example.domain.InventoryCalculator
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    STOCK_LOW_TO_HIGH("Stock: Low to High"),
    STOCK_HIGH_TO_LOW("Stock: High to Low"),
    TURNOVER_RATE("Turnover Velocity"),
    NAME_AZ("Name: A to Z"),
    PROFIT_MARGIN("Profit Margin %")
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    val allBeverages: StateFlow<List<BeverageItem>>
    val allVendors: StateFlow<List<Vendor>>
    val allTransactions: StateFlow<List<InventoryTransaction>>
    val allOrders: StateFlow<List<PurchaseOrder>>

    // Filter and Search states
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("All") // "All", "Pepsi Products", "Low Stock", "Overdue 10+d", "Cola", "Citrus", etc.
    val selectedSort = MutableStateFlow(SortOption.STOCK_LOW_TO_HIGH)

    // Theme mode state
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Notification message events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = InventoryRepository(
            beverageDao = database.beverageDao(),
            vendorDao = database.vendorDao(),
            transactionDao = database.transactionDao(),
            purchaseOrderDao = database.purchaseOrderDao()
        )

        allBeverages = repository.allBeverages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allVendors = repository.allVendors.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Analytics calculations derived from beverages and transactions
    val analyticsMap: StateFlow<Map<Long, BeverageAnalytics>> = combine(
        allBeverages,
        allTransactions
    ) { beverages, transactions ->
        val now = System.currentTimeMillis()
        beverages.associate { item ->
            item.id to InventoryCalculator.computeBeverageAnalytics(item, transactions, now)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Store aggregate summary
    val storeSummary: StateFlow<StoreInventorySummary> = combine(
        allBeverages,
        allTransactions
    ) { beverages, transactions ->
        InventoryCalculator.computeStoreSummary(beverages, transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreInventorySummary()
    )

    // Overdue stock alerts (> 10 days inactive)
    val overdueAlerts: StateFlow<List<OverdueStockAlert>> = allBeverages.combine(
        MutableStateFlow(System.currentTimeMillis())
    ) { beverages, _ ->
        InventoryCalculator.generateOverdueAlerts(beverages)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered and sorted beverages for inventory view
    val filteredBeverages: StateFlow<List<BeverageItem>> = combine(
        allBeverages,
        searchQuery,
        selectedFilter,
        selectedSort,
        analyticsMap
    ) { beverages, query, filter, sort, analytics ->
        val now = System.currentTimeMillis()
        var result = beverages

        // 1. Filter by category/special condition
        result = when (filter) {
            "All" -> result
            "Pepsi" -> result.filter { it.brand.contains("Pepsi", ignoreCase = true) }
            "Low Stock" -> result.filter { it.isLowStock }
            "Overdue 10+d" -> result.filter { it.isOverdueForDays(now, 10) }
            "Cola" -> result.filter { it.category.contains("Cola", ignoreCase = true) }
            "Citrus" -> result.filter { it.category.contains("Citrus", ignoreCase = true) }
            "Specialty" -> result.filter { it.category.contains("Specialty", ignoreCase = true) || it.category.contains("Root Beer", ignoreCase = true) }
            "Water & Sports" -> result.filter { it.category.contains("Water", ignoreCase = true) || it.category.contains("Sports", ignoreCase = true) }
            else -> result.filter { it.category.equals(filter, ignoreCase = true) }
        }

        // 2. Filter by search query (name, brand, SKU)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.sku.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }

        // 3. Sort
        when (sort) {
            SortOption.STOCK_LOW_TO_HIGH -> result.sortedBy { it.currentStockUnits }
            SortOption.STOCK_HIGH_TO_LOW -> result.sortedByDescending { it.currentStockUnits }
            SortOption.TURNOVER_RATE -> result.sortedByDescending { analytics[it.id]?.turnoverRate ?: 0.0 }
            SortOption.NAME_AZ -> result.sortedBy { it.name.lowercase() }
            SortOption.PROFIT_MARGIN -> result.sortedByDescending { it.marginPercentage }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Items that have automated reorder recommendations
    val reorderItems: StateFlow<List<BeverageAnalytics>> = analyticsMap.combine(allBeverages) { analytics, _ ->
        analytics.values.filter { it.isReorderRecommended && it.suggestedOrderCases > 0 }
            .sortedBy { it.beverage.currentStockUnits }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilter(filter: String) {
        selectedFilter.value = filter
    }

    fun setSort(sort: SortOption) {
        selectedSort.value = sort
    }

    fun recordQuickSale(beverage: BeverageItem, units: Int = 1) {
        viewModelScope.launch {
            if (beverage.currentStockUnits <= 0) {
                _userMessage.emit("${beverage.name} is out of stock!")
                return@launch
            }
            val actualUnits = units.coerceAtMost(beverage.currentStockUnits)
            repository.recordSale(beverage.id, actualUnits, beverage.sellingPrice)
            _userMessage.emit("Sold $actualUnits unit(s) of ${beverage.name}")
        }
    }

    fun recordQuickRestock(beverage: BeverageItem, cases: Int = 1) {
        viewModelScope.launch {
            val totalUnits = cases * beverage.unitsPerCase
            repository.recordRestock(beverage.id, totalUnits, beverage.buyingPrice)
            _userMessage.emit("Restocked $cases case(s) (+$totalUnits units) of ${beverage.name}")
        }
    }

    fun recordCustomSale(beverageId: Long, units: Int, customPrice: Double?, notes: String) {
        viewModelScope.launch {
            repository.recordSale(beverageId, units, customPrice, notes)
            _userMessage.emit("Sale recorded successfully")
        }
    }

    fun recordCustomRestock(beverageId: Long, units: Int, customUnitCost: Double?, notes: String) {
        viewModelScope.launch {
            repository.recordRestock(beverageId, units, customUnitCost, notes)
            _userMessage.emit("Restock logged successfully")
        }
    }

    fun recordAdjustment(beverageId: Long, newCount: Int, reason: String) {
        viewModelScope.launch {
            repository.recordAdjustment(beverageId, newCount, reason)
            _userMessage.emit("Inventory count updated to $newCount")
        }
    }

    fun recordDamaged(beverageId: Long, units: Int, notes: String) {
        viewModelScope.launch {
            repository.recordDamagedLoss(beverageId, units, notes)
            _userMessage.emit("Logged $units damaged/spoiled units")
        }
    }

    fun saveBeverage(beverage: BeverageItem) {
        viewModelScope.launch {
            if (beverage.id == 0L) {
                repository.insertBeverage(beverage)
                _userMessage.emit("Added ${beverage.name} to inventory")
            } else {
                repository.updateBeverage(beverage)
                _userMessage.emit("Updated ${beverage.name}")
            }
        }
    }

    fun deleteBeverage(beverage: BeverageItem) {
        viewModelScope.launch {
            repository.deleteBeverage(beverage)
            _userMessage.emit("Removed ${beverage.name} from catalog")
        }
    }

    // Vendor & Order actions
    fun saveVendor(vendor: Vendor) {
        viewModelScope.launch {
            if (vendor.id == 0L) {
                repository.insertVendor(vendor)
                _userMessage.emit("Added vendor ${vendor.name}")
            } else {
                repository.updateVendor(vendor)
                _userMessage.emit("Updated vendor ${vendor.name}")
            }
        }
    }

    fun deleteVendor(vendor: Vendor) {
        viewModelScope.launch {
            repository.deleteVendor(vendor)
            _userMessage.emit("Deleted vendor ${vendor.name}")
        }
    }

    fun updateOrderStatus(orderId: Long, status: PurchaseOrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            _userMessage.emit("Order status updated to ${status.name}")
        }
    }

    /**
     * Automated Reorder generator: creates a consolidated Purchase Order for all recommended items
     * from a specific vendor or across vendors based on turnover and low stock calculations.
     */
    fun generateAutomatedReorderPO(vendor: Vendor, itemsToOrder: List<BeverageAnalytics>) {
        viewModelScope.launch {
            if (itemsToOrder.isEmpty()) {
                _userMessage.emit("No items need reordering for ${vendor.name}")
                return@launch
            }
            val totalCases = itemsToOrder.sumOf { it.suggestedOrderCases }
            val totalCost = itemsToOrder.sumOf { it.estimatedOrderCost }
            val summary = itemsToOrder.joinToString(", ") {
                "${it.beverage.name} (${it.suggestedOrderCases} cs @ $${String.format("%.2f", it.beverage.wholesalePrice)})"
            }
            repository.createPurchaseOrder(
                vendorId = vendor.id,
                vendorName = vendor.name,
                totalCases = totalCases,
                totalEstimatedCost = totalCost,
                itemsSummary = summary,
                leadTimeDays = vendor.deliveryLeadDays,
                notes = "Auto-generated replenishment based on sales velocity and safety thresholds."
            )
            _userMessage.emit("Created Purchase Order for ${vendor.name} ($totalCases cases)")
        }
    }

    fun resetCatalogToDefaults() {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication(), viewModelScope)
            AppDatabase.populateInitialData(db)
            _userMessage.emit("Catalog refreshed with default soda inventory")
        }
    }
}

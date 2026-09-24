package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BeverageAnalytics
import com.example.data.model.BeverageItem
import com.example.data.model.Vendor
import com.example.ui.components.AddEditBeverageDialog
import com.example.ui.components.AddEditVendorDialog
import com.example.ui.components.AdjustStockDialog
import com.example.ui.components.QuickRestockDialog
import com.example.ui.components.QuickSaleDialog
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.PepsiBluePrimary
import com.example.ui.theme.SodaCyan
import com.example.ui.theme.SodaGreen
import com.example.ui.theme.SodaOrange
import com.example.ui.theme.SodaRed
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    INVENTORY("Inventory", Icons.Default.Inventory2),
    ANALYTICS("Turnover", Icons.Default.TrendingUp),
    REORDERS("Orders", Icons.Default.LocalShipping),
    HISTORY("Reports", Icons.Default.ReceiptLong)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Dialog state
    var selectedBeverageForSale by remember { mutableStateOf<BeverageItem?>(null) }
    var selectedBeverageForRestock by remember { mutableStateOf<BeverageItem?>(null) }
    var selectedBeverageForAdjust by remember { mutableStateOf<BeverageItem?>(null) }
    var editingBeverage by remember { mutableStateOf<BeverageItem?>(null) }
    var isAddingNewBeverage by remember { mutableStateOf(false) }
    var editingVendor by remember { mutableStateOf<Vendor?>(null) }
    var isAddingNewVendor by remember { mutableStateOf(false) }

    // Observe ViewModel States
    val beverages by viewModel.filteredBeverages.collectAsStateWithLifecycle()
    val allBeveragesList by viewModel.allBeverages.collectAsStateWithLifecycle()
    val vendors by viewModel.allVendors.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val purchaseOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val analyticsMap by viewModel.analyticsMap.collectAsStateWithLifecycle()
    val storeSummary by viewModel.storeSummary.collectAsStateWithLifecycle()
    val overdueAlerts by viewModel.overdueAlerts.collectAsStateWithLifecycle()
    val reorderItems by viewModel.reorderItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PepsiBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Column {
                            Text(
                                text = "SodaTrack",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Pepsi & Retail Soda Inventory",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Overdue alert indicator badge
                    if (storeSummary.overdueStockCount > 0) {
                        IconButton(
                            onClick = {
                                viewModel.setFilter("Overdue 10+d")
                                currentTab = ScreenTab.INVENTORY.ordinal
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = SodaRed) {
                                        Text("${storeSummary.overdueStockCount}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "Overdue 10d Alert",
                                    tint = SodaRed
                                )
                            }
                        }
                    }

                    // Theme toggle button
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                AppThemeMode.LIGHT -> AppThemeMode.DARK
                                AppThemeMode.DARK -> AppThemeMode.SYSTEM
                                AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                            }
                            viewModel.setThemeMode(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.SYSTEM -> Icons.Default.DarkMode
                            },
                            contentDescription = "Toggle Theme"
                        )
                    }

                    // More Menu
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Restore Default Catalog") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            },
                            onClick = {
                                viewModel.resetCatalogToDefaults()
                                menuExpanded = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                ScreenTab.values().forEachIndexed { index, tab ->
                    val selected = currentTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = index },
                        icon = {
                            if (tab == ScreenTab.INVENTORY && storeSummary.lowStockCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = SodaOrange) {
                                            Text("${storeSummary.lowStockCount}")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else if (tab == ScreenTab.REORDERS && reorderItems.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = PepsiBluePrimary) {
                                            Text("${reorderItems.size}")
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PepsiBluePrimary,
                            selectedTextColor = PepsiBluePrimary,
                            indicatorColor = PepsiBluePrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "tab_transition",
            modifier = Modifier.padding(innerPadding)
        ) { tabIndex ->
            when (ScreenTab.values()[tabIndex]) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(
                        summary = storeSummary,
                        overdueAlerts = overdueAlerts,
                        analyticsList = analyticsMap.values.toList(),
                        recentTransactions = transactions,
                        onNavigateToInventoryWithFilter = { filter ->
                            viewModel.setFilter(filter)
                            currentTab = ScreenTab.INVENTORY.ordinal
                        },
                        onNavigateToReorders = { currentTab = ScreenTab.REORDERS.ordinal },
                        onNavigateToAnalytics = { currentTab = ScreenTab.ANALYTICS.ordinal },
                        onAddNewBeverage = { isAddingNewBeverage = true },
                        onQuickSaleClick = { selectedBeverageForSale = it }
                    )
                }

                ScreenTab.INVENTORY -> {
                    InventoryScreen(
                        beverages = beverages,
                        analyticsMap = analyticsMap,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedFilter = selectedFilter,
                        onFilterChange = { viewModel.setFilter(it) },
                        selectedSort = selectedSort,
                        onSortChange = { viewModel.setSort(it) },
                        onQuickSale = { viewModel.recordQuickSale(it, 1) },
                        onQuickRestock = { viewModel.recordQuickRestock(it, 1) },
                        onAdjustStock = { selectedBeverageForAdjust = it },
                        onCardClick = { editingBeverage = it },
                        onAddNewBeverage = { isAddingNewBeverage = true }
                    )
                }

                ScreenTab.ANALYTICS -> {
                    TurnoverAnalyticsScreen(
                        summary = storeSummary,
                        analyticsList = analyticsMap.values.toList(),
                        overdueAlerts = overdueAlerts,
                        onBeverageClick = { id ->
                            allBeveragesList.find { it.id == id }?.let { editingBeverage = it }
                        }
                    )
                }

                ScreenTab.REORDERS -> {
                    VendorOrdersScreen(
                        vendors = vendors,
                        reorderItems = reorderItems,
                        purchaseOrders = purchaseOrders,
                        onGeneratePO = { vendor, items ->
                            viewModel.generateAutomatedReorderPO(vendor, items)
                        },
                        onUpdateOrderStatus = { id, status ->
                            viewModel.updateOrderStatus(id, status)
                        },
                        onAddNewVendor = { isAddingNewVendor = true },
                        onEditVendor = { editingVendor = it }
                    )
                }

                ScreenTab.HISTORY -> {
                    HistoryReportsScreen(
                        transactions = transactions,
                        summary = storeSummary
                    )
                }
            }
        }
    }

    // Quick Sale Dialog
    selectedBeverageForSale?.let { beverage ->
        QuickSaleDialog(
            beverage = beverage,
            onDismiss = { selectedBeverageForSale = null },
            onConfirm = { units, customPrice, notes ->
                viewModel.recordCustomSale(beverage.id, units, customPrice, notes)
                selectedBeverageForSale = null
            }
        )
    }

    // Quick Restock Dialog
    selectedBeverageForRestock?.let { beverage ->
        QuickRestockDialog(
            beverage = beverage,
            onDismiss = { selectedBeverageForRestock = null },
            onConfirm = { units, customUnitCost, notes ->
                viewModel.recordCustomRestock(beverage.id, units, customUnitCost, notes)
                selectedBeverageForRestock = null
            }
        )
    }

    // Adjust Stock Count Dialog
    selectedBeverageForAdjust?.let { beverage ->
        AdjustStockDialog(
            beverage = beverage,
            onDismiss = { selectedBeverageForAdjust = null },
            onConfirm = { newCount, reason ->
                viewModel.recordAdjustment(beverage.id, newCount, reason)
                selectedBeverageForAdjust = null
            }
        )
    }

    // Add or Edit Beverage Dialog
    if (isAddingNewBeverage || editingBeverage != null) {
        AddEditBeverageDialog(
            beverage = editingBeverage,
            vendors = vendors,
            onDismiss = {
                isAddingNewBeverage = false
                editingBeverage = null
            },
            onSave = { item ->
                viewModel.saveBeverage(item)
                isAddingNewBeverage = false
                editingBeverage = null
            },
            onDelete = { item ->
                viewModel.deleteBeverage(item)
                isAddingNewBeverage = false
                editingBeverage = null
            }
        )
    }

    // Add or Edit Vendor Dialog
    if (isAddingNewVendor || editingVendor != null) {
        AddEditVendorDialog(
            vendor = editingVendor,
            onDismiss = {
                isAddingNewVendor = false
                editingVendor = null
            },
            onSave = { v ->
                viewModel.saveVendor(v)
                isAddingNewVendor = false
                editingVendor = null
            },
            onDelete = { v ->
                viewModel.deleteVendor(v)
                isAddingNewVendor = false
                editingVendor = null
            }
        )
    }
}

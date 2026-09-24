package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BeverageAnalytics
import com.example.data.model.BeverageItem
import com.example.data.model.InventoryTransaction
import com.example.data.model.OverdueStockAlert
import com.example.data.model.StoreInventorySummary
import com.example.data.model.TransactionType
import com.example.data.model.VelocityTier
import com.example.ui.components.MetricCard
import com.example.ui.theme.PepsiBluePrimary
import com.example.ui.theme.PepsiRed
import com.example.ui.theme.SodaAmber
import com.example.ui.theme.SodaCyan
import com.example.ui.theme.SodaGreen
import com.example.ui.theme.SodaOrange
import com.example.ui.theme.SodaRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    summary: StoreInventorySummary,
    overdueAlerts: List<OverdueStockAlert>,
    analyticsList: List<BeverageAnalytics>,
    recentTransactions: List<InventoryTransaction>,
    onNavigateToInventoryWithFilter: (String) -> Unit,
    onNavigateToReorders: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onAddNewBeverage: () -> Unit,
    onQuickSaleClick: (BeverageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val fastMovers = analyticsList.filter { it.velocityCategory == VelocityTier.FAST }.take(4)
    val slowMovers = analyticsList.filter { it.velocityCategory == VelocityTier.SLOW || it.velocityCategory == VelocityTier.STAGNANT }.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_beverage_hero),
                        contentDescription = "Beverage retail shelf",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PepsiBluePrimary.copy(alpha = 0.92f),
                                        Color(0xFF001845).copy(alpha = 0.70f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SodaCyan.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "RETAIL SODA & PEPSI INVENTORY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SodaCyan,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Real-Time Stock & Velocity",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "${summary.totalBeverageCount} Active SKUs • ${summary.totalUnitsOnHand} Units (${summary.totalCasesOnHand} Cases) on Floor",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddNewBeverage,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PepsiBluePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_dashboard_add")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Soda", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = { onNavigateToInventoryWithFilter("Low Stock") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SodaOrange.copy(alpha = 0.15f),
                        contentColor = SodaOrange
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Low Stock (${summary.lowStockCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onNavigateToReorders,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reorder", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CRITICAL ALERT: OVERDUE STOCK >= 10 DAYS
        if (summary.overdueStockCount > 0) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .testTag("banner_overdue_alerts")
                        .clickable { onNavigateToInventoryWithFilter("Overdue 10+d") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = SodaRed.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SodaRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = "Overdue Alert",
                                        tint = SodaRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "OVERDUE STOCK ALERT (10+ DAYS)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SodaRed,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Text(
                                        text = "${summary.overdueStockCount} beverage items have had ZERO sales for 10+ days!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Overdue Items",
                                tint = SodaRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Show preview of top overdue item
                        overdueAlerts.firstOrNull()?.let { alert ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alert.beverage.name,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${alert.daysInactive} days inactive • $${String.format("%.2f", alert.tiedUpCapital)} tied up capital",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Text(
                                            text = "Action: ${alert.suggestedAction}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SodaRed,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 10.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Valuation & Profit Matrix Grid (Wholesale, Buying, Selling)
        item {
            Text(
                text = "Store Inventory Valuation & Pricing",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Cost Valuation",
                    value = "$${String.format("%.2f", summary.totalCostValuation)}",
                    subtitle = "Valued at buying prices",
                    icon = Icons.Default.AttachMoney,
                    iconTint = SodaCyan,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Retail Valuation",
                    value = "$${String.format("%.2f", summary.totalRetailValuation)}",
                    subtitle = "Potential sales revenue",
                    icon = Icons.Default.TrendingUp,
                    iconTint = SodaGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Potential Profit",
                    value = "$${String.format("%.2f", summary.totalPotentialProfit)}",
                    subtitle = "Gross profit margin in stock",
                    icon = Icons.Default.Receipt,
                    iconTint = PepsiBluePrimary,
                    badgeText = "${String.format("%.1f", summary.averageProfitMarginPercent)}% Margin",
                    badgeColor = SodaGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Wholesale Value",
                    value = "$${String.format("%.2f", summary.totalWholesaleValuation)}",
                    subtitle = "Distributor case rate",
                    icon = Icons.Default.Inventory2,
                    iconTint = SodaAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Sales Ticker
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SodaGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = SodaGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Today's Beverage Sales",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${summary.totalSalesTodayUnits} units sold today",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Text(
                        text = "$${String.format("%.2f", summary.totalRevenueToday)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SodaGreen
                        )
                    )
                }
            }
        }

        // Fast-Moving Beverages Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = SodaAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Fast Movers (High Turnover)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "View Analytics",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { onNavigateToAnalytics() }
                )
            }
        }

        items(fastMovers) { stats ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stats.beverage.name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${stats.beverage.brand} • ${stats.beverage.packageType}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SodaGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${stats.turnoverRate}x/yr",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SodaGreen
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${stats.averageDailySales} units/day",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }

        // Recent Activity Feed
        item {
            Text(
                text = "Recent Inventory Logs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(recentTransactions.take(5)) { tx ->
            val isSale = tx.type == TransactionType.SALE
            val isRestock = tx.type == TransactionType.RESTOCK
            val timeStr = SimpleDateFormat("h:mm a • MMM d", Locale.US).format(Date(tx.timestamp))

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSale -> SodaGreen.copy(alpha = 0.15f)
                                        isRestock -> PepsiBluePrimary.copy(alpha = 0.15f)
                                        else -> SodaOrange.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isSale -> Icons.Default.ShoppingCart
                                    isRestock -> Icons.Default.Inventory2
                                    else -> Icons.Default.Receipt
                                },
                                contentDescription = null,
                                tint = when {
                                    isSale -> SodaGreen
                                    isRestock -> PepsiBluePrimary
                                    else -> SodaOrange
                                },
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Column {
                            Text(
                                text = tx.beverageName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${tx.notes} • $timeStr",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Text(
                        text = if (isSale) "-${kotlin.math.abs(tx.unitsChanged)}" else "+${tx.unitsChanged}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSale) PepsiRed else SodaGreen
                        )
                    )
                }
            }
        }
    }
}

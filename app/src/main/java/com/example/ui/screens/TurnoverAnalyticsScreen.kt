package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeverageAnalytics
import com.example.data.model.OverdueStockAlert
import com.example.data.model.StoreInventorySummary
import com.example.data.model.VelocityTier
import com.example.ui.components.MetricCard
import com.example.ui.theme.PepsiBluePrimary
import com.example.ui.theme.SodaAmber
import com.example.ui.theme.SodaCyan
import com.example.ui.theme.SodaGreen
import com.example.ui.theme.SodaOrange
import com.example.ui.theme.SodaRed

@Composable
fun TurnoverAnalyticsScreen(
    summary: StoreInventorySummary,
    analyticsList: List<BeverageAnalytics>,
    overdueAlerts: List<OverdueStockAlert>,
    onBeverageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Turnover Velocity", "10d Overdue Stock", "Margin Matrix")

    val fastMovers = analyticsList.filter { it.velocityCategory == VelocityTier.FAST }
    val steadyMovers = analyticsList.filter { it.velocityCategory == VelocityTier.MEDIUM }
    val slowMovers = analyticsList.filter { it.velocityCategory == VelocityTier.SLOW }
    val stagnantItems = analyticsList.filter { it.isOverdueTenDays }

    val totalTiedUpOverdue = overdueAlerts.sumOf { it.tiedUpCapital }
    val avgTurnover = if (analyticsList.isNotEmpty()) {
        analyticsList.map { it.turnoverRate }.average()
    } else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Row
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PepsiBluePrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // TAB 0: TURNOVER VELOCITY & SALES TRENDS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Avg Store Turnover",
                            value = "${String.format("%.1f", avgTurnover)}x / yr",
                            subtitle = "Annualized inventory turns",
                            icon = Icons.Default.Speed,
                            iconTint = SodaCyan,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Stock Velocity Ratio",
                            value = "${fastMovers.size} Fast / ${slowMovers.size} Slow",
                            subtitle = "ABC inventory breakdown",
                            icon = Icons.Default.AutoGraph,
                            iconTint = SodaGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Inventory Turnover Explanation Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PepsiBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Inventory Turnover Rate (ITR) Formula",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "ITR = (Annualized Units Sold) ÷ (Current Stock Units). High turnover (>6x) indicates fresh, fast-selling stock like Pepsi 12oz cans. Low turnover (<2x) signals overstocked or lagging shelf items.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // Section: Fast-Moving Class A Items
                item {
                    Text(
                        text = "Class A: Fast-Moving Beverages (${fastMovers.size} items)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SodaGreen
                        )
                    )
                }

                items(fastMovers) { stats ->
                    VelocityItemCard(stats = stats, badgeColor = SodaGreen, onClick = { onBeverageClick(stats.beverage.id) })
                }

                // Section: Class B: Steady Performers
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Class B: Steady Performers (${steadyMovers.size} items)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PepsiBluePrimary
                        )
                    )
                }

                items(steadyMovers) { stats ->
                    VelocityItemCard(stats = stats, badgeColor = PepsiBluePrimary, onClick = { onBeverageClick(stats.beverage.id) })
                }

                // Section: Class C: Slow Movers
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Class C: Slow-Moving Stock (${slowMovers.size} items)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SodaOrange
                        )
                    )
                }

                items(slowMovers) { stats ->
                    VelocityItemCard(stats = stats, badgeColor = SodaOrange, onClick = { onBeverageClick(stats.beverage.id) })
                }
            }

            1 -> {
                // TAB 1: 10+ DAYS OVERDUE STOCK ALERTS (Stagnant / Dead Stock)
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = SodaRed.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SodaRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = SodaRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${overdueAlerts.size} Overdue Stagnant Products",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SodaRed
                                        )
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", totalTiedUpOverdue)} tied up in unmoving store stock (>= 10 days)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Stagnant Inventory Overdue for 10+ Days",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Calculated from last recorded sales and inventory movement. Immediate action required to free shelf space and capital.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                if (overdueAlerts.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SodaGreen.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SodaGreen)
                                Text(
                                    text = "All soda stock is healthy! No products overdue past 10 days.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SodaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                items(overdueAlerts) { alert ->
                    ElevatedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBeverageClick(alert.beverage.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = alert.beverage.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SodaRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${alert.daysInactive} DAYS OVERDUE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SodaRed,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${alert.beverage.packageType} • ${alert.beverage.currentStockUnits} units in stock",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tied Capital: $${String.format("%.2f", alert.tiedUpCapital)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Wholesale Cs: $${String.format("%.2f", alert.beverage.wholesalePrice)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SodaAmber.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = SodaAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = alert.suggestedAction,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: PRICING MATRIX (Buying vs Wholesale vs Retail Selling)
                item {
                    Text(
                        text = "Wholesale, Buying & Retail Selling Price Matrix",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Margin analysis shows store markup % and gross profit yield per unit and per wholesale case.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                items(analyticsList) { stats ->
                    val bev = stats.beverage
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBeverageClick(bev.id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bev.name,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SodaGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", bev.marginPercentage)}% Margin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SodaGreen
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${bev.packageType} (${bev.unitsPerCase} units/cs)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Buying Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "$${String.format("%.2f", bev.buyingPrice)}/unit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Column {
                                    Text(text = "Case Wholesale", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "$${String.format("%.2f", bev.wholesalePrice)}/cs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Column {
                                    Text(text = "Retail Selling", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "$${String.format("%.2f", bev.sellingPrice)}/unit", fontWeight = FontWeight.Bold, color = PepsiBluePrimary, fontSize = 12.sp)
                                }
                                Column {
                                    Text(text = "Case Profit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "+$${String.format("%.2f", bev.fullCaseProfit)}", fontWeight = FontWeight.ExtraBold, color = SodaGreen, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VelocityItemCard(
    stats: BeverageAnalytics,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    text = "Stock: ${stats.beverage.currentStockUnits} units • DOIR: ${if (stats.daysOfInventoryRemaining > 100) "Stagnant" else "${stats.daysOfInventoryRemaining} days"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${stats.turnoverRate}x/yr",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${stats.averageDailySales} sold/day",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                )
            }
        }
    }
}

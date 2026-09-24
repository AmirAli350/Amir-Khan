package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryTransaction
import com.example.data.model.StoreInventorySummary
import com.example.data.model.TransactionType
import com.example.ui.components.MetricCard
import com.example.ui.theme.PepsiBluePrimary
import com.example.ui.theme.PepsiRed
import com.example.ui.theme.SodaCyan
import com.example.ui.theme.SodaGreen
import com.example.ui.theme.SodaOrange
import com.example.ui.theme.SodaRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryReportsScreen(
    transactions: List<InventoryTransaction>,
    summary: StoreInventorySummary,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredTransactions = when (selectedFilter) {
        "Sales" -> transactions.filter { it.type == TransactionType.SALE }
        "Restocks" -> transactions.filter { it.type == TransactionType.RESTOCK }
        "Adjustments" -> transactions.filter { it.type == TransactionType.AUDIT_ADJUSTMENT || it.type == TransactionType.SPOILAGE_DAMAGED }
        else -> transactions
    }

    val totalRevenueAllTime = transactions
        .filter { it.type == TransactionType.SALE }
        .sumOf { it.totalAmount }

    val totalUnitsSold = transactions
        .filter { it.type == TransactionType.SALE }
        .sumOf { kotlin.math.abs(it.unitsChanged) }

    val totalUnitsRestocked = transactions
        .filter { it.type == TransactionType.RESTOCK }
        .sumOf { it.unitsChanged }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial & Activity KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Historical Sales",
                    value = "$${String.format("%.2f", totalRevenueAllTime)}",
                    subtitle = "$totalUnitsSold total units sold",
                    icon = Icons.Default.AttachMoney,
                    iconTint = SodaGreen,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Received Stock",
                    value = "$totalUnitsRestocked Units",
                    subtitle = "Delivered from vendors",
                    icon = Icons.Default.Inventory2,
                    iconTint = PepsiBluePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Export Full Report Button
        item {
            ElevatedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Inventory Report",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Generate comprehensive store valuation, sales trends & overdue stock audit summary.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val report = generateInventoryReportText(summary, transactions)
                            shareReportText(context, report)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PepsiBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Sales", "Restocks", "Adjustments").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Transaction Count Header
        item {
            Text(
                text = "Transaction History (${filteredTransactions.size} records)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions logged for this filter.")
                    }
                }
            }
        }

        items(filteredTransactions, key = { it.id }) { tx ->
            val isSale = tx.type == TransactionType.SALE
            val isRestock = tx.type == TransactionType.RESTOCK
            val isDamage = tx.type == TransactionType.SPOILAGE_DAMAGED
            val isAudit = tx.type == TransactionType.AUDIT_ADJUSTMENT

            val tintColor = when {
                isSale -> SodaGreen
                isRestock -> PepsiBluePrimary
                isDamage -> SodaRed
                else -> SodaOrange
            }

            val icon = when {
                isSale -> Icons.Default.ShoppingCart
                isRestock -> Icons.Default.Inventory2
                isDamage -> Icons.Default.DeleteSweep
                else -> Icons.Default.Edit
            }

            val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(Date(tx.timestamp))

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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tintColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tintColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = tx.beverageName,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = tx.notes,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isSale || isDamage) "-${kotlin.math.abs(tx.unitsChanged)} units" else "+${tx.unitsChanged} units",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSale) SodaGreen else tintColor
                            )
                        )
                        Text(
                            text = "$${String.format("%.2f", kotlin.math.abs(tx.totalAmount))}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun generateInventoryReportText(
    summary: StoreInventorySummary,
    transactions: List<InventoryTransaction>
): String {
    val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
    return buildString {
        appendLine("==============================================")
        appendLine("SODATRACK: BEVERAGE & SODA INVENTORY REPORT")
        appendLine("Generated: $nowStr")
        appendLine("==============================================")
        appendLine()
        appendLine("1. STORE VALUATION & PRICING")
        appendLine("----------------------------------------------")
        appendLine("Total Active Beverage SKUs: ${summary.totalBeverageCount}")
        appendLine("Total Units on Hand: ${summary.totalUnitsOnHand} (${summary.totalCasesOnHand} cases)")
        appendLine("Total Inventory Cost (Buying Price): $${String.format("%.2f", summary.totalCostValuation)}")
        appendLine("Total Wholesale Valuation: $${String.format("%.2f", summary.totalWholesaleValuation)}")
        appendLine("Total Retail Potential Value: $${String.format("%.2f", summary.totalRetailValuation)}")
        appendLine("Potential Gross Margin: $${String.format("%.2f", summary.totalPotentialProfit)} (${String.format("%.1f", summary.averageProfitMarginPercent)}%)")
        appendLine()
        appendLine("2. ALERTS & RISK AUDIT")
        appendLine("----------------------------------------------")
        appendLine("Low Stock Alert Items: ${summary.lowStockCount}")
        appendLine("Overdue Stock Items (>= 10 Days Stagnant): ${summary.overdueStockCount}")
        appendLine("Today's Units Sold: ${summary.totalSalesTodayUnits}")
        appendLine("Today's Gross Revenue: $${String.format("%.2f", summary.totalRevenueToday)}")
        appendLine()
        appendLine("3. HISTORICAL ACTIVITY")
        appendLine("----------------------------------------------")
        appendLine("Total Logged Transactions: ${transactions.size}")
        appendLine("==============================================")
    }
}

private fun shareReportText(context: Context, report: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "SodaTrack Store Inventory Report")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Share Inventory Report"))
}

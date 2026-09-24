package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeverageAnalytics
import com.example.data.model.BeverageItem
import com.example.ui.theme.Brand7UpColor
import com.example.ui.theme.BrandMtnDewColor
import com.example.ui.theme.BrandPepsiColor
import com.example.ui.theme.PepsiBluePrimary
import com.example.ui.theme.PepsiRed
import com.example.ui.theme.SodaCyan
import com.example.ui.theme.SodaGreen
import com.example.ui.theme.SodaOrange
import com.example.ui.theme.SodaRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeverageCard(
    beverage: BeverageItem,
    analytics: BeverageAnalytics?,
    onQuickSale: () -> Unit,
    onQuickRestockCase: () -> Unit,
    onAdjustStock: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockPercentage = if (beverage.targetStockUnits > 0) {
        (beverage.currentStockUnits.toFloat() / beverage.targetStockUnits.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val stockStatusColor = when {
        beverage.isOutOfStock -> SodaRed
        beverage.isLowStock -> SodaOrange
        else -> SodaGreen
    }

    val brandTagColor = when {
        beverage.brand.contains("Pepsi", ignoreCase = true) -> BrandPepsiColor
        beverage.brand.contains("Dew", ignoreCase = true) -> BrandMtnDewColor
        beverage.brand.contains("7Up", ignoreCase = true) -> Brand7UpColor
        else -> SodaCyan
    }

    val isOverdue = beverage.isOverdueForDays()

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("beverage_card_${beverage.id}")
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Brand badge, category, and Stock Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(brandTagColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = beverage.brand.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = brandTagColor,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = "• ${beverage.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Stock Health Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = stockStatusColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(stockStatusColor)
                        )
                        Text(
                            text = when {
                                beverage.isOutOfStock -> "Out of Stock"
                                beverage.isLowStock -> "Low Stock"
                                else -> "Healthy Stock"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = stockStatusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Beverage Name and Package Type
            Text(
                text = beverage.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "${beverage.packageType} • SKU: ${beverage.sku}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Overdue Alert Banner if inactive for >= 10 days
            if (isOverdue) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SodaRed.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Overdue Alert",
                            tint = SodaRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Alert: Stock overdue ${beverage.daysSinceLastSale()}d without sales! (Tied: $${String.format("%.2f", beverage.totalCostValue)})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SodaRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Stock Level & Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${beverage.currentStockUnits}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (beverage.isLowStock) stockStatusColor else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "units on hand",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Text(
                        text = "= ${beverage.casesOnHand} full cases + ${beverage.looseUnitsOnHand} loose (${beverage.unitsPerCase}/cs)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Velocity badge
                analytics?.let { stats ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${stats.turnoverRate}x/yr turnover",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "${stats.averageDailySales} units/day",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { stockPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = stockStatusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Matrix: Buying, Wholesale, Selling, Margin
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Buying",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$${String.format("%.2f", beverage.buyingPrice)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Wholesale/Cs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$${String.format("%.2f", beverage.wholesalePrice)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Retail Selling",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$${String.format("%.2f", beverage.sellingPrice)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Margin",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "${String.format("%.0f", beverage.marginPercentage)}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SodaGreen
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Interactive Buttons Row: -1 Sale, +1 Unit, +Case Restock, Adjust
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Sale -1
                FilledTonalButton(
                    onClick = onQuickSale,
                    enabled = beverage.currentStockUnits > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_sale_${beverage.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PepsiBluePrimary.copy(alpha = 0.12f),
                        contentColor = PepsiBluePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Record Sale",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sale -1",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Quick Restock +1 Case
                FilledTonalButton(
                    onClick = onQuickRestockCase,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_restock_${beverage.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SodaGreen.copy(alpha = 0.12f),
                        contentColor = SodaGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Restock Case",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+1 Case",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Quick Audit / Adjust count icon button
                OutlinedIconButton(
                    onClick = onAdjustStock,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_adjust_${beverage.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Adjust Count",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

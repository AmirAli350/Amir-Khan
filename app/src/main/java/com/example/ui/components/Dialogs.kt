package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BeverageItem
import com.example.data.model.Vendor
import com.example.ui.theme.SodaRed

@Composable
fun QuickSaleDialog(
    beverage: BeverageItem,
    onDismiss: () -> Unit,
    onConfirm: (units: Int, customPrice: Double?, notes: String) -> Unit
) {
    var unitsText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(String.format("%.2f", beverage.sellingPrice)) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Sale - ${beverage.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Current Stock: ${beverage.currentStockUnits} units (${beverage.casesOnHand} cases)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = unitsText,
                    onValueChange = { unitsText = it },
                    label = { Text("Units Sold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sale_units")
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 6, 12, beverage.unitsPerCase).forEach { count ->
                        OutlinedButton(
                            onClick = { unitsText = count.toString() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "$count", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Selling Price per Unit ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val units = unitsText.toIntOrNull() ?: 1
                    val price = priceText.toDoubleOrNull() ?: beverage.sellingPrice
                    onConfirm(units, price, notes)
                },
                modifier = Modifier.testTag("btn_confirm_sale")
            ) {
                Text("Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickRestockDialog(
    beverage: BeverageItem,
    onDismiss: () -> Unit,
    onConfirm: (units: Int, customUnitCost: Double?, notes: String) -> Unit
) {
    var casesText by remember { mutableStateOf("1") }
    var looseUnitsText by remember { mutableStateOf("0") }
    var wholesaleCaseCostText by remember { mutableStateOf(String.format("%.2f", beverage.wholesalePrice)) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Restock Delivery - ${beverage.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Standard Case Size: ${beverage.unitsPerCase} units/case",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = casesText,
                    onValueChange = { casesText = it },
                    label = { Text("Number of Cases Received") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_restock_cases")
                )

                // Quick case presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 5, 10).forEach { cs ->
                        OutlinedButton(
                            onClick = { casesText = cs.toString() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "${cs}cs", fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = looseUnitsText,
                    onValueChange = { looseUnitsText = it },
                    label = { Text("Additional Loose Units (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wholesaleCaseCostText,
                    onValueChange = { wholesaleCaseCostText = it },
                    label = { Text("Wholesale Cost per Case ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Invoice / PO # / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cases = casesText.toIntOrNull() ?: 0
                    val loose = looseUnitsText.toIntOrNull() ?: 0
                    val totalUnits = (cases * beverage.unitsPerCase) + loose
                    val caseCost = wholesaleCaseCostText.toDoubleOrNull() ?: beverage.wholesalePrice
                    val unitCost = if (beverage.unitsPerCase > 0) caseCost / beverage.unitsPerCase else beverage.buyingPrice
                    onConfirm(totalUnits, unitCost, notes)
                },
                modifier = Modifier.testTag("btn_confirm_restock")
            ) {
                Text("Receive Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdjustStockDialog(
    beverage: BeverageItem,
    onDismiss: () -> Unit,
    onConfirm: (newCount: Int, reason: String) -> Unit
) {
    var countText by remember { mutableStateOf(beverage.currentStockUnits.toString()) }
    var reasonText by remember { mutableStateOf("Cycle count physical audit") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Audit Stock Count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${beverage.name} (${beverage.packageType})\nCurrent in system: ${beverage.currentStockUnits} units",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it },
                    label = { Text("Actual Physical Count (units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_count")
                )

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason for Adjustment") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = countText.toIntOrNull() ?: beverage.currentStockUnits
                    onConfirm(count, reasonText)
                },
                modifier = Modifier.testTag("btn_confirm_adjust")
            ) {
                Text("Update Count")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBeverageDialog(
    beverage: BeverageItem?,
    vendors: List<Vendor>,
    onDismiss: () -> Unit,
    onSave: (BeverageItem) -> Unit,
    onDelete: ((BeverageItem) -> Unit)? = null
) {
    var name by remember { mutableStateOf(beverage?.name ?: "") }
    var brand by remember { mutableStateOf(beverage?.brand ?: "Pepsi") }
    var category by remember { mutableStateOf(beverage?.category ?: "Cola") }
    var sku by remember { mutableStateOf(beverage?.sku ?: "PEP-") }
    var packageType by remember { mutableStateOf(beverage?.packageType ?: "12 oz Can (24/Case)") }
    var unitsPerCaseText by remember { mutableStateOf((beverage?.unitsPerCase ?: 24).toString()) }
    var currentStockText by remember { mutableStateOf((beverage?.currentStockUnits ?: 24).toString()) }
    var minThresholdText by remember { mutableStateOf((beverage?.minStockThresholdUnits ?: 12).toString()) }
    var targetStockText by remember { mutableStateOf((beverage?.targetStockUnits ?: 48).toString()) }
    var buyingPriceText by remember { mutableStateOf(String.format("%.2f", beverage?.buyingPrice ?: 0.45)) }
    var wholesalePriceText by remember { mutableStateOf(String.format("%.2f", beverage?.wholesalePrice ?: 10.80)) }
    var sellingPriceText by remember { mutableStateOf(String.format("%.2f", beverage?.sellingPrice ?: 1.25)) }
    var selectedVendorId by remember { mutableStateOf(beverage?.vendorId ?: (vendors.firstOrNull()?.id ?: 1L)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (beverage == null) "Add Beverage Item" else "Edit Beverage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Pepsi Wild Cherry") },
                    modifier = Modifier.fillMaxWidth().testTag("input_beverage_name")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Barcode") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = packageType,
                        onValueChange = { packageType = it },
                        label = { Text("Package Type") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitsPerCaseText,
                        onValueChange = { unitsPerCaseText = it },
                        label = { Text("Units / Case") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentStockText,
                        onValueChange = { currentStockText = it },
                        label = { Text("Current Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minThresholdText,
                        onValueChange = { minThresholdText = it },
                        label = { Text("Min Alert Level") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetStockText,
                        onValueChange = { targetStockText = it },
                        label = { Text("Par Target Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Pricing Structure (Cost, Wholesale, Retail)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyingPriceText,
                        onValueChange = { buyingPriceText = it },
                        label = { Text("Unit Cost ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wholesalePriceText,
                        onValueChange = { wholesalePriceText = it },
                        label = { Text("Case Wholesale ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = it },
                    label = { Text("Retail Selling Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_selling_price")
                )

                // Margin indicator preview
                val buy = buyingPriceText.toDoubleOrNull() ?: 0.0
                val sell = sellingPriceText.toDoubleOrNull() ?: 0.0
                if (sell > 0) {
                    val margin = ((sell - buy) / sell) * 100
                    Text(
                        text = "Calculated Gross Margin: ${String.format("%.1f", margin)}% (Profit: $${String.format("%.2f", sell - buy)}/unit)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Delete button if editing existing
                if (beverage != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onDelete(beverage) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SodaRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Beverage from Catalog")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val item = BeverageItem(
                        id = beverage?.id ?: 0L,
                        sku = sku.ifBlank { "SKU-${System.currentTimeMillis() % 10000}" },
                        name = name.trim(),
                        brand = brand.trim().ifBlank { "Generic" },
                        category = category.trim().ifBlank { "Soda" },
                        packageType = packageType.trim(),
                        unitsPerCase = unitsPerCaseText.toIntOrNull() ?: 24,
                        currentStockUnits = currentStockText.toIntOrNull() ?: 0,
                        minStockThresholdUnits = minThresholdText.toIntOrNull() ?: 12,
                        targetStockUnits = targetStockText.toIntOrNull() ?: 48,
                        buyingPrice = buyingPriceText.toDoubleOrNull() ?: 0.50,
                        wholesalePrice = wholesalePriceText.toDoubleOrNull() ?: 12.00,
                        sellingPrice = sellingPriceText.toDoubleOrNull() ?: 1.25,
                        vendorId = selectedVendorId,
                        lastRestockedTimestamp = beverage?.lastRestockedTimestamp ?: System.currentTimeMillis(),
                        lastSoldTimestamp = beverage?.lastSoldTimestamp ?: System.currentTimeMillis()
                    )
                    onSave(item)
                },
                modifier = Modifier.testTag("btn_save_beverage")
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditVendorDialog(
    vendor: Vendor?,
    onDismiss: () -> Unit,
    onSave: (Vendor) -> Unit,
    onDelete: ((Vendor) -> Unit)? = null
) {
    var name by remember { mutableStateOf(vendor?.name ?: "") }
    var contact by remember { mutableStateOf(vendor?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(vendor?.phone ?: "") }
    var email by remember { mutableStateOf(vendor?.email ?: "") }
    var terms by remember { mutableStateOf(vendor?.paymentTerms ?: "Net 30") }
    var leadDaysText by remember { mutableStateOf((vendor?.deliveryLeadDays ?: 3).toString()) }
    var notes by remember { mutableStateOf(vendor?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (vendor == null) "Add Beverage Vendor" else "Edit Vendor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Distributor / Vendor Name *") },
                    placeholder = { Text("e.g. PBNA Direct") },
                    modifier = Modifier.fillMaxWidth().testTag("input_vendor_name")
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Sales Rep / Contact Person") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Payment Terms") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = leadDaysText,
                        onValueChange = { leadDaysText = it },
                        label = { Text("Lead Time (Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Delivery Schedule / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (vendor != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onDelete(vendor) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SodaRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Vendor")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val v = Vendor(
                        id = vendor?.id ?: 0L,
                        name = name.trim(),
                        contactPerson = contact.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        paymentTerms = terms.trim(),
                        deliveryLeadDays = leadDaysText.toIntOrNull() ?: 3,
                        notes = notes.trim()
                    )
                    onSave(v)
                },
                modifier = Modifier.testTag("btn_save_vendor")
            ) {
                Text("Save Vendor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BeverageItem
import com.example.data.model.InventoryTransaction
import com.example.data.model.TransactionType
import com.example.domain.InventoryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SodaTrack", appName)
  }

  @Test
  fun `verify inventory turnover rate and overdue 10 days calculation`() {
    val now = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L

    // Stagnant item with last sale 14 days ago
    val stagnantPepsi = BeverageItem(
      id = 1,
      sku = "PEP-TEST",
      name = "Pepsi Nitro",
      brand = "Pepsi",
      category = "Cola",
      packageType = "12 oz Can (24/Case)",
      unitsPerCase = 24,
      currentStockUnits = 20,
      minStockThresholdUnits = 10,
      targetStockUnits = 48,
      buyingPrice = 0.50,
      wholesalePrice = 12.00,
      sellingPrice = 1.25,
      lastSoldTimestamp = now - (14 * dayMs)
    )

    assertTrue("Item inactive for 14 days must be overdue for 10+ days", stagnantPepsi.isOverdueForDays(now, 10))
    assertEquals(14, stagnantPepsi.daysSinceLastSale(now))

    // Active item with recent sales
    val activeDew = BeverageItem(
      id = 2,
      sku = "DEW-TEST",
      name = "Mountain Dew",
      brand = "Mountain Dew",
      category = "Citrus Soda",
      packageType = "12 oz Can (24/Case)",
      unitsPerCase = 24,
      currentStockUnits = 48,
      minStockThresholdUnits = 24,
      targetStockUnits = 96,
      buyingPrice = 0.45,
      wholesalePrice = 10.80,
      sellingPrice = 1.25,
      lastSoldTimestamp = now - (2 * 3600 * 1000L)
    )

    assertFalse("Item sold 2 hours ago must not be overdue", activeDew.isOverdueForDays(now, 10))

    // Transactions: 70 units sold over past 14 days
    val transactions = listOf(
      InventoryTransaction(
        beverageId = 2,
        beverageName = "Mountain Dew",
        type = TransactionType.SALE,
        unitsChanged = -70,
        unitPrice = 1.25,
        totalAmount = 87.50,
        timestamp = now - (3 * dayMs)
      )
    )

    val analytics = InventoryCalculator.computeBeverageAnalytics(activeDew, transactions, now)
    assertEquals(5.0, analytics.averageDailySales, 0.1) // 70 / 14 = 5.0 units/day
    // Annualized turnover = (5.0 * 365) / 48 = 38.0x
    assertTrue("Turnover rate should reflect high velocity", analytics.turnoverRate > 10.0)

    // Verify pricing margin
    assertEquals(0.80, activeDew.unitProfit, 0.01)
    assertEquals(64.0, activeDew.marginPercentage, 0.1)
  }
}

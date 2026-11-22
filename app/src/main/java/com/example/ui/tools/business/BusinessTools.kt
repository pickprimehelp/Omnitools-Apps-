package com.example.ui.tools.business

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.util.ImageExportUtils
import java.util.Locale
import kotlin.math.pow

data class InvoiceItem(
    var name: String,
    var qty: Double,
    var rate: Double,
    var gstRate: Double = 18.0
) {
    val totalTaxable: Double get() = qty * rate
    val taxAmount: Double get() = totalTaxable * (gstRate / 100.0)
    val totalAmount: Double get() = totalTaxable + taxAmount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceBillMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var businessName by remember { mutableStateOf("OmniTools Store") }
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var gstNumber by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemRate by remember { mutableStateOf("") }
    var itemGst by remember { mutableDoubleStateOf(18.0) }

    val items = remember {
        mutableStateListOf(
            InvoiceItem("Service / Product", 1.0, 500.0, 18.0)
        )
    }

    val grandTaxable = items.sumOf { it.totalTaxable }
    val grandTax = items.sumOf { it.taxAmount }
    val grandTotal = items.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice & Bill Maker", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / Shop Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it },
                        label = { Text("GSTIN (Optional)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add Line Item", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Item Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = itemQty,
                                onValueChange = { itemQty = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = itemRate,
                                onValueChange = { itemRate = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Rate ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.0, 5.0, 12.0, 18.0, 28.0).forEach { slab ->
                                FilterChip(
                                    selected = itemGst == slab,
                                    onClick = { itemGst = slab },
                                    label = { Text("${slab.toInt()}%", fontSize = 11.sp) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val qty = itemQty.toDoubleOrNull() ?: 1.0
                                val rate = itemRate.toDoubleOrNull() ?: 0.0
                                if (itemName.isNotBlank() && rate > 0.0) {
                                    items.add(InvoiceItem(itemName.trim(), qty, rate, itemGst))
                                    itemName = ""
                                    itemRate = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Item")
                        }
                    }
                }
            }
            itemsIndexed(items) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "${item.qty} × ₹${item.rate} + ${item.gstRate.toInt()}% GST",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "₹${String.format(Locale.US, "%.2f", item.totalAmount)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { items.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Item")
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Taxable Subtotal: ₹${String.format(Locale.US, "%.2f", grandTaxable)}")
                        Text("Total GST: ₹${String.format(Locale.US, "%.2f", grandTax)}")
                        Text(
                            "Grand Total: ₹${String.format(Locale.US, "%.2f", grandTotal)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val sb = StringBuilder()
                                sb.appendLine("INVOICE - $businessName")
                                if (gstNumber.isNotBlank()) sb.appendLine("GSTIN: $gstNumber")
                                sb.appendLine("Customer: $customerName")
                                sb.appendLine("-------------------------")
                                items.forEachIndexed { idx, it ->
                                    sb.appendLine("${idx + 1}. ${it.name} (${it.qty} × ₹${it.rate}, GST ${it.gstRate.toInt()}%) = ₹${String.format(Locale.US, "%.2f", it.totalAmount)}")
                                }
                                sb.appendLine("-------------------------")
                                sb.appendLine("Taxable: ₹${String.format(Locale.US, "%.2f", grandTaxable)}")
                                sb.appendLine("GST: ₹${String.format(Locale.US, "%.2f", grandTax)}")
                                sb.appendLine("Grand Total: ₹${String.format(Locale.US, "%.2f", grandTotal)}")
                                ImageExportUtils.shareText(context, sb.toString(), "Share Invoice")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Invoice Summary")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var amountText by remember { mutableStateOf("1000") }
    var gstRate by remember { mutableDoubleStateOf(18.0) }
    var isInclusive by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val netPrice: Double
    val gstAmount: Double
    val totalAmount: Double
    if (isInclusive) {
        netPrice = amount / (1.0 + gstRate / 100.0)
        gstAmount = amount - netPrice
        totalAmount = amount
    } else {
        netPrice = amount
        gstAmount = amount * (gstRate / 100.0)
        totalAmount = amount + gstAmount
    }
    val halfGst = gstAmount / 2.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GST & Tax Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isInclusive,
                        onClick = { isInclusive = false },
                        label = { Text("Add GST (Exclusive)") }
                    )
                    FilterChip(
                        selected = isInclusive,
                        onClick = { isInclusive = true },
                        label = { Text("Remove GST (Inclusive)") }
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3.0, 5.0, 12.0, 18.0, 28.0).forEach { slab ->
                        FilterChip(
                            selected = gstRate == slab,
                            onClick = { gstRate = slab },
                            label = { Text("${slab.toInt()}%") }
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Net Amount: ₹${String.format(Locale.US, "%.2f", netPrice)}")
                        Text("CGST (${gstRate / 2}%): ₹${String.format(Locale.US, "%.2f", halfGst)}")
                        Text("SGST (${gstRate / 2}%): ₹${String.format(Locale.US, "%.2f", halfGst)}")
                        Text("Total GST: ₹${String.format(Locale.US, "%.2f", gstAmount)}")
                        Text(
                            "Final Amount: ₹${String.format(Locale.US, "%.2f", totalAmount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var principalText by remember { mutableStateOf("500000") }
    var rateText by remember { mutableStateOf("9.5") }
    var monthsText by remember { mutableStateOf("36") }

    val principal = principalText.toDoubleOrNull() ?: 0.0
    val annualRate = rateText.toDoubleOrNull() ?: 0.0
    val months = (monthsText.toIntOrNull() ?: 1).coerceAtLeast(1)

    val r = annualRate / 12.0 / 100.0
    val emi = if (r > 0.0) {
        val factor = (1.0 + r).pow(months)
        (principal * r * factor) / (factor - 1.0)
    } else {
        principal / months
    }
    val totalPayable = emi * months
    val totalInterest = (totalPayable - principal).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI & Loan Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = principalText,
                    onValueChange = { principalText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Loan Principal (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Interest % (p.a.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = monthsText,
                        onValueChange = { monthsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Tenure (Months)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Monthly EMI: ₹${String.format(Locale.US, "%.2f", emi)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Principal Amount: ₹${String.format(Locale.US, "%.2f", principal)}")
                        Text("Total Interest: ₹${String.format(Locale.US, "%.2f", totalInterest)}")
                        Text("Total Payment: ₹${String.format(Locale.US, "%.2f", totalPayable)}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitDiscountCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var costOrPriceText by remember { mutableStateOf("1000") }
    var sellOrDiscountText by remember { mutableStateOf("1250") }
    var discountPercentText by remember { mutableStateOf("15") }

    val cost = costOrPriceText.toDoubleOrNull() ?: 0.0
    val sell = sellOrDiscountText.toDoubleOrNull() ?: 0.0
    val profit = sell - cost
    val marginPct = if (sell > 0.0) (profit / sell) * 100.0 else 0.0
    val markupPct = if (cost > 0.0) (profit / cost) * 100.0 else 0.0

    val discPct = discountPercentText.toDoubleOrNull() ?: 0.0
    val discAmount = cost * (discPct / 100.0)
    val finalDiscPrice = (cost - discAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profit, Discount & % Calc", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = costOrPriceText,
                        onValueChange = { costOrPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Cost / MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellOrDiscountText,
                        onValueChange = { sellOrDiscountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Selling Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Profit Margin Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Net Profit: ₹${String.format(Locale.US, "%.2f", profit)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Profit Margin: ${String.format(Locale.US, "%.2f", marginPct)}%")
                        Text("Markup: ${String.format(Locale.US, "%.2f", markupPct)}%")
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = discountPercentText,
                    onValueChange = { discountPercentText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Discount / Tip Percentage (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Discount / Percentage Result", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Savings ($discPct%): ₹${String.format(Locale.US, "%.2f", discAmount)}")
                        Text(
                            "Final Price: ₹${String.format(Locale.US, "%.2f", finalDiscPrice)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

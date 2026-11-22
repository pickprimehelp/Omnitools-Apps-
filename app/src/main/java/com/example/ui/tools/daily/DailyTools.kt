package com.example.ui.tools.daily

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.util.BarcodeQrUtils
import com.example.util.ImageExportUtils
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeGeneratorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("https://play.google.com") }
    var isBarcode by remember { mutableStateOf(false) }

    val bitmap: Bitmap? = remember(inputText, isBarcode) {
        if (isBarcode) {
            BarcodeQrUtils.generateBarcodeBitmap(inputText)
        } else {
            BarcodeQrUtils.generateQrBitmap(inputText)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR & Barcode Generator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !isBarcode, onClick = { isBarcode = false }, label = { Text("QR Code") })
                    FilterChip(selected = isBarcode, onClick = { isBarcode = true }, label = { Text("Barcode (Code 128)") })
                }
            }
            item {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter URL, UPI ID, WiFi or Text") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = if (isBarcode) "Generated Barcode" else "Generated QR Code",
                            modifier = Modifier.size(if (isBarcode) 260.dp else 220.dp)
                        )
                    } else {
                        Text("Enter text to generate code", color = Color.Gray)
                    }
                }
            }
            if (bitmap != null) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { ImageExportUtils.saveBitmapToGallery(context, bitmap, "OmniTools_Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = { ImageExportUtils.shareBitmap(context, bitmap, "OmniTools QR Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("1") }
    var birthDay by remember { mutableStateOf("1") }

    val now = Calendar.getInstance()
    val y = (birthYear.toIntOrNull() ?: 2000).coerceIn(1900, now.get(Calendar.YEAR))
    val m = (birthMonth.toIntOrNull() ?: 1).coerceIn(1, 12)
    val d = (birthDay.toIntOrNull() ?: 1).coerceIn(1, 31)

    val dob = Calendar.getInstance().apply { set(y, m - 1, d, 0, 0, 0) }
    var years = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
    var days = now.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)
    if (days < 0) {
        months--
        days += 30
    }
    if (months < 0) {
        years--
        months += 12
    }
    val totalDays = ((now.timeInMillis - dob.timeInMillis) / (1000L * 60 * 60 * 24)).coerceAtLeast(0L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Age & Date Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = birthDay,
                        onValueChange = { birthDay = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthMonth,
                        onValueChange = { birthMonth = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Month") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthYear,
                        onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
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
                            text = "${years.coerceAtLeast(0)} Years, $months Months, $days Days",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Total Days Lived: $totalDays days")
                        Text("Total Hours: ${totalDays * 24} hours")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var weightKg by remember { mutableStateOf("68") }
    var heightCm by remember { mutableStateOf("172") }

    val w = weightKg.toDoubleOrNull() ?: 0.0
    val hMeters = (heightCm.toDoubleOrNull() ?: 0.0) / 100.0
    val bmi = if (hMeters > 0.0) w / (hMeters * hMeters) else 0.0
    val category = when {
        bmi <= 0.0 -> "Enter valid height & weight"
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Normal & Healthy"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BMI Health Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Height (cm)") },
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "BMI: ${String.format(Locale.US, "%.1f", bmi)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Category: $category", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchAndTimerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<String>() }

    LaunchedEffect(running) {
        var lastTick = System.currentTimeMillis()
        while (running) {
            delay(50L)
            val now = System.currentTimeMillis()
            elapsedMs += (now - lastTick)
            lastTick = now
        }
    }

    fun formatMs(ms: Long): String {
        val mins = (ms / 60000L)
        val secs = (ms % 60000L) / 1000L
        val hundredths = (ms % 1000L) / 10L
        return String.format(Locale.US, "%02d:%02d.%02d", mins, secs, hundredths)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stopwatch & Timer", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatMs(elapsedMs),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { running = !running }) {
                                Text(if (running) "Pause" else "Start")
                            }
                            OutlinedButton(
                                onClick = { if (elapsedMs > 0L) laps.add(0, "Lap ${laps.size + 1}: ${formatMs(elapsedMs)}") },
                                enabled = elapsedMs > 0L
                            ) {
                                Text("Lap")
                            }
                            OutlinedButton(onClick = {
                                running = false
                                elapsedMs = 0L
                                laps.clear()
                            }) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
            items(laps) { lap ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(lap, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var valueText by remember { mutableStateOf("10") }
    var selectedType by remember { mutableIntStateOf(0) }
    val types = listOf("Length (m → ft)", "Weight (kg → lb)", "Temp (°C → °F)", "Currency (USD → INR)")

    val v = valueText.toDoubleOrNull() ?: 0.0
    val result = when (selectedType) {
        0 -> "${String.format(Locale.US, "%.2f", v * 3.28084)} ft"
        1 -> "${String.format(Locale.US, "%.2f", v * 2.20462)} lb"
        2 -> "${String.format(Locale.US, "%.2f", (v * 9.0 / 5.0) + 32.0)} °F"
        else -> "₹${String.format(Locale.US, "%.2f", v * 84.5)}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit & Currency Converter", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedType == idx,
                            onClick = { selectedType = idx },
                            label = { Text(label) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                    label = { Text("Input Value") },
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
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Converted Output", fontSize = 13.sp)
                        Text(
                            text = result,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

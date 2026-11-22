package com.example.ui.tools.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.TopBannerAd
import com.example.util.ImageExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale

private fun decodeSafeBitmap(context: Context, uri: Uri, maxDim: Int = 1600): Bitmap? {
    return try {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        var sample = 1
        while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) {
            sample *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        }
    } catch (_: Exception) {
        null
    }
}

private fun compressBitmap(source: Bitmap, quality: Int): Pair<Bitmap, Int> {
    val stream = ByteArrayOutputStream()
    source.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(5, 100), stream)
    val bytes = stream.toByteArray()
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: source
    return decoded to (bytes.size / 1024)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedKb by remember { mutableIntStateOf(0) }
    var quality by remember { mutableFloatStateOf(65f) }
    var scalePercent by remember { mutableFloatStateOf(100f) }

    fun recompute(bmp: Bitmap?, q: Float, scale: Float) {
        if (bmp == null) return
        val scaled = if (scale < 99f) {
            val w = (bmp.width * (scale / 100f)).toInt().coerceAtLeast(32)
            val h = (bmp.height * (scale / 100f)).toInt().coerceAtLeast(32)
            Bitmap.createScaledBitmap(bmp, w, h, true)
        } else {
            bmp
        }
        val (out, kb) = compressBitmap(scaled, q.toInt())
        compressedBitmap = out
        compressedKb = kb
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val decoded = decodeSafeBitmap(context, uri)
                withContext(Dispatchers.Main) {
                    sourceBitmap = decoded
                    recompute(decoded, quality, scalePercent)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Compressor & Resizer", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Button(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Photo from Gallery")
                }
            }

            if (compressedBitmap != null) {
                item {
                    Image(
                        bitmap = compressedBitmap!!.asImageBitmap(),
                        contentDescription = "Compressed Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.06f))
                    )
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Output Size: $compressedKb KB • ${compressedBitmap!!.width} × ${compressedBitmap!!.height} px",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Quality: ${quality.toInt()}%")
                            Slider(
                                value = quality,
                                onValueChange = {
                                    quality = it
                                    recompute(sourceBitmap, it, scalePercent)
                                },
                                valueRange = 10f..100f
                            )
                            Text("Resize Scale: ${scalePercent.toInt()}%")
                            Slider(
                                value = scalePercent,
                                onValueChange = {
                                    scalePercent = it
                                    recompute(sourceBitmap, quality, it)
                                },
                                valueRange = 25f..100f
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                ImageExportUtils.saveBitmapToGallery(context, compressedBitmap!!, "OmniTools_Compressed")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = {
                                ImageExportUtils.shareBitmap(context, compressedBitmap!!, "Compressed Photo")
                            },
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
fun PassportPhotoMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bgColor by remember { mutableStateOf(Color.White) }
    val bgChoices = listOf(
        Color.White,
        Color(0xFFDBEAFE),
        Color(0xFF2563EB),
        Color(0xFFF3F4F6)
    )

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = decodeSafeBitmap(context, uri, 1000)
                withContext(Dispatchers.Main) { photoBitmap = bmp }
            }
        }
    }

    fun buildPassportSheet(photo: Bitmap, bgArgb: Int): Bitmap {
        val sheetW = 1200
        val sheetH = 1000
        val sheet = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(android.graphics.Color.WHITE)

        val cellW = 350
        val cellH = 450
        val cols = 3
        val rows = 2
        val gapX = 35
        val gapY = 35
        val startX = (sheetW - (cols * cellW + (cols - 1) * gapX)) / 2
        val startY = (sheetH - (rows * cellH + (rows - 1) * gapY)) / 2

        val bgPaint = Paint().apply { color = bgArgb }
        val borderPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = startX + c * (cellW + gapX)
                val top = startY + r * (cellH + gapY)
                val rect = Rect(left, top, left + cellW, top + cellH)
                canvas.drawRect(rect, bgPaint)
                canvas.drawBitmap(photo, null, rect, Paint(Paint.ANTI_ALIAS_FLAG))
                canvas.drawRect(rect, borderPaint)
            }
        }
        return sheet
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passport Photo & 6-Sheet", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
        val sheet = remember(photoBitmap, bgColor) {
            photoBitmap?.let { buildPassportSheet(it, bgColor.toArgb()) }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Portrait Photo")
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Background:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    bgChoices.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (bgColor == color) 3.dp else 1.dp,
                                    color = if (bgColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { bgColor = color }
                        )
                    }
                }
            }
            if (sheet != null) {
                item {
                    Image(
                        bitmap = sheet.asImageBitmap(),
                        contentDescription = "6-Photo Passport Print Sheet",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { ImageExportUtils.saveBitmapToGallery(context, sheet, "PassportSheet") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Sheet")
                        }
                        OutlinedButton(
                            onClick = { ImageExportUtils.shareBitmap(context, sheet, "Passport Photo Sheet") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Sheet")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageQualityCheckerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var aspectMode by remember { mutableStateOf("Original") }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val bmp = decodeSafeBitmap(context, uri, 2000)
                withContext(Dispatchers.Main) { selectedBitmap = bmp }
            }
        }
    }

    val processedBitmap = remember(selectedBitmap, aspectMode) {
        val bmp = selectedBitmap ?: return@remember null
        when (aspectMode) {
            "1:1 Square" -> cropCenterAspect(bmp, 1f)
            "16:9 Wide" -> cropCenterAspect(bmp, 16f / 9f)
            "4:5 Portrait" -> cropCenterAspect(bmp, 4f / 5f)
            else -> bmp
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Crop & Quality Inspector", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Button(
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Image to Inspect / Crop")
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Original", "1:1 Square", "16:9 Wide", "4:5 Portrait").forEach { mode ->
                        FilterChip(
                            selected = aspectMode == mode,
                            onClick = { aspectMode = mode },
                            label = { Text(mode, fontSize = 11.sp) }
                        )
                    }
                }
            }
            if (processedBitmap != null) {
                val mp = (processedBitmap.width * processedBitmap.height) / 1_000_000.0
                val rating = when {
                    processedBitmap.width >= 1080 && processedBitmap.height >= 1080 -> "HD Print Ready (300 DPI up to 4x6\")"
                    processedBitmap.width >= 720 -> "Good for Social Media & Web"
                    else -> "Low Resolution (Best for Icons/Web)"
                }
                item {
                    Image(
                        bitmap = processedBitmap.asImageBitmap(),
                        contentDescription = "Inspected Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.06f))
                    )
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Resolution: ${processedBitmap.width} × ${processedBitmap.height} px", fontWeight = FontWeight.Bold)
                            Text("Megapixels: ${String.format(Locale.US, "%.2f", mp)} MP")
                            Text("Quality Rating: $rating", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { ImageExportUtils.saveBitmapToGallery(context, processedBitmap, "OmniTools_Crop") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = { ImageExportUtils.shareBitmap(context, processedBitmap, "OmniTools Image") },
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

private fun cropCenterAspect(src: Bitmap, targetAspect: Float): Bitmap {
    val srcAspect = src.width.toFloat() / src.height.toFloat()
    val (cropW, cropH) = if (srcAspect > targetAspect) {
        (src.height * targetAspect).toInt() to src.height
    } else {
        src.width to (src.width / targetAspect).toInt()
    }
    val x = ((src.width - cropW) / 2).coerceAtLeast(0)
    val y = ((src.height - cropH) / 2).coerceAtLeast(0)
    return Bitmap.createBitmap(src, x, y, cropW.coerceAtMost(src.width - x), cropH.coerceAtMost(src.height - y))
}

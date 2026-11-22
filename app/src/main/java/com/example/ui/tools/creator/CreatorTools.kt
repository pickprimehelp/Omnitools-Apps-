package com.example.ui.tools.creator

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeTitleGeneratorScreen(
    isShortsMode: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var topic by remember { mutableStateOf("") }
    var niche by remember { mutableStateOf("Tech & Tutorials") }
    val niches = listOf("Tech & Tutorials", "Vlogs", "Gaming", "Finance", "Motivation")

    val cleanTopic = topic.trim().ifBlank { "Android Productivity" }
    val generatedTitles = remember(cleanTopic, niche, isShortsMode) {
        if (isShortsMode) {
            listOf(
                "$cleanTopic Hack You Missed! 🔥",
                "Stop Doing $cleanTopic Wrong! ❌",
                "60s $cleanTopic Secret ⚡",
                "Best $cleanTopic Trick in 2026 🤯",
                "$cleanTopic: Watch Before Deleted! 👀",
                "How Pros Do $cleanTopic 🚀"
            )
        } else {
            listOf(
                "How to Master $cleanTopic in 2026 (Step-by-Step Guide)",
                "7 $cleanTopic Mistakes Beginners Make & How to Fix Them",
                "I Tested $cleanTopic for 30 Days — Honest Results!",
                "The Ultimate $cleanTopic Blueprint for $niche",
                "Why Everyone Is Switching to $cleanTopic Right Now",
                "Complete $cleanTopic Tutorial: Beginner to Pro"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isShortsMode) "Shorts Title Generator" else "YouTube Title & Caption Pro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
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
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Enter Video Topic / Keyword") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    niches.take(3).forEach { item ->
                        FilterChip(
                            selected = niche == item,
                            onClick = { niche = item },
                            label = { Text(item, fontSize = 12.sp) }
                        )
                    }
                }
            }
            items(generatedTitles) { title ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${title.length} chars", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { ImageExportUtils.copyToClipboard(context, title, "Title") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagAndTagGeneratorScreen(
    isTagMode: Boolean = false,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var keyword by remember { mutableStateOf("creator") }
    val slug = keyword.trim().lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]"), "").ifBlank { "trending" }

    val tags = remember(slug, isTagMode) {
        val base = listOf(
            slug, "${slug}tips", "${slug}2026", "${slug}tutorial", "${slug}guide",
            "viral$slug", "${slug}hacks", "best$slug", "${slug}india", "trending",
            "reels", "youtubeshorts", "explore", "creator", "viralshorts"
        )
        if (isTagMode) base.joinToString(", ") else base.joinToString(" ") { "#$it" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isTagMode) "SEO Video Tag Generator" else "Viral Hashtag Generator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
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
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Enter Niche or Keyword") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isTagMode) "Comma-Separated Tags (${tags.length}/500)" else "Ready-to-Paste Hashtags",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(tags, fontSize = 14.sp, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { ImageExportUtils.copyToClipboard(context, tags, "Tags") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy All")
                            }
                            Button(
                                onClick = { ImageExportUtils.shareText(context, tags, "Share Tags") },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeRevenueCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var dailyViews by remember { mutableStateOf("15000") }
    var rpmSlider by remember { mutableFloatStateOf(1.8f) }

    val views = dailyViews.toDoubleOrNull() ?: 0.0
    val dailyRev = (views / 1000.0) * rpmSlider
    val monthlyRev = dailyRev * 30.0
    val yearlyRev = dailyRev * 365.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("YouTube Revenue & RPM", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                    value = dailyViews,
                    onValueChange = { dailyViews = it.filter { c -> c.isDigit() } },
                    label = { Text("Daily Views") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column {
                    Text(
                        text = "Estimated RPM: $${String.format(Locale.US, "%.2f", rpmSlider)} per 1K views",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Slider(
                        value = rpmSlider,
                        onValueChange = { rpmSlider = it },
                        valueRange = 0.2f..15f
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Estimated Earnings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Daily: $${String.format(Locale.US, "%.2f", dailyRev)}", fontSize = 15.sp)
                        Text("Monthly (30d): $${String.format(Locale.US, "%.2f", monthlyRev)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("Yearly (365d): $${String.format(Locale.US, "%.2f", yearlyRev)}", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFileSizeCalculatorScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    var durationMinutes by remember { mutableStateOf("10") }
    var bitrateMbps by remember { mutableStateOf("12") }
    var resolution by remember { mutableStateOf("1080p Full HD (1920x1080)") }

    val resolutions = listOf(
        "720p HD (1280x720)",
        "1080p Full HD (1920x1080)",
        "4K UHD (3840x2160)"
    )

    val mins = durationMinutes.toDoubleOrNull() ?: 0.0
    val mbps = bitrateMbps.toDoubleOrNull() ?: 0.0
    val sizeMb = (mins * 60.0 * mbps) / 8.0
    val sizeGb = sizeMb / 1024.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video & Thumbnail Size Tool", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    resolutions.forEach { res ->
                        FilterChip(
                            selected = resolution == res,
                            onClick = {
                                resolution = res
                                bitrateMbps = when {
                                    res.startsWith("720p") -> "6"
                                    res.startsWith("4K") -> "35"
                                    else -> "12"
                                }
                            },
                            label = { Text(res) }
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Duration (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bitrateMbps,
                        onValueChange = { bitrateMbps = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Bitrate (Mbps)") },
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
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estimated File Size", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", sizeMb)} MB (${String.format(Locale.US, "%.2f", sizeGb)} GB)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "YouTube Thumbnail Spec: 1280 × 720 px (16:9), max 2 MB (JPG/PNG). Keep bottom-right corner clear for video duration badge.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

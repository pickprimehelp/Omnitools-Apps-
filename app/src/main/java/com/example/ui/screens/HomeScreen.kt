package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.TopBannerAd
import com.example.ads.findActivity
import com.example.model.ToolCategory
import com.example.model.ToolItem
import com.example.model.ToolRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSelectTool: (String) -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ToolCategory.ALL) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableIntStateOf(5) }

    val navigateWithAd: (String) -> Unit = { toolId ->
        if (activity != null) {
            AdManager.showInterstitialIfReady(activity, forceShow = false) {
                onSelectTool(toolId)
            }
        } else {
            onSelectTool(toolId)
        }
    }

    val featuredTools = remember { ToolRegistry.allTools.filter { it.isFeatured } }
    val filteredTools = remember(searchQuery, selectedCategory) {
        ToolRegistry.allTools.filter { tool ->
            val matchesCategory = selectedCategory == ToolCategory.ALL || tool.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                tool.title.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchVisible) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search 40+ tools...", fontSize = 12.sp) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchVisible = false
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close Search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_tools_input")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "OmniTools",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (!isSearchVisible) {
                        IconButton(
                            onClick = { isSearchVisible = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search Tools")
                        }
                    }
                    IconButton(
                        onClick = { shareApp(context) },
                        modifier = Modifier.testTag("share_app_button")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share App",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showRatingDialog = true },
                        modifier = Modifier.testTag("rate_app_button")
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rate App",
                            tint = Color(0xFFF59E0B)
                        )
                    }
                    IconButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier.testTag("privacy_button")
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Privacy Policy",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("about_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "About App")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = { TopBannerAd() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "ALL-IN-ONE SMART SUITE ✨",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "40+ Powerful Free Tools",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Design cards, edit photos, convert PDFs, calculate business finance & create viral content offline.",
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Featured & Trending",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredTools, key = { it.id }) { tool ->
                                FeaturedToolCard(
                                    tool = tool,
                                    onClick = { navigateWithAd(tool.id) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ToolCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.badgeColor.copy(alpha = 0.16f),
                                selectedLabelColor = category.badgeColor
                            ),
                            modifier = Modifier.testTag("category_${category.name.lowercase()}")
                        )
                    }
                }
            }

            item {
                Text(
                    text = if (searchQuery.isNotBlank()) {
                        "Results (${filteredTools.size})"
                    } else {
                        "${selectedCategory.title} (${filteredTools.size})"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(filteredTools.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = { navigateWithAd(tool.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Love OmniTools? Rate Us 5 Stars!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your review helps us build even more free tools and keep the app fast and reliable.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { openPlayStoreRating(context) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review on Google Play Store", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Share OmniTools With Friends",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Help your friends save storage space by replacing 20+ single-purpose apps with OmniTools.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { shareApp(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share App Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Rate OmniTools", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("How has your experience been with OmniTools?", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { userRating = star }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= userRating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRatingDialog = false
                        openPlayStoreRating(context)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Review on Google Play", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About OmniTools Pro", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "OmniTools combines 40+ design, creator, business, image, PDF, voice, and daily utility tools in one fast offline-ready app.",
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = {
                            showInfoDialog = false
                            shareApp(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share OmniTools App")
                    }
                    OutlinedButton(
                        onClick = {
                            showInfoDialog = false
                            showPrivacyDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Read Privacy Policy")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "All image, PDF, audio, and calculator processing happens directly on your device. " +
                        "Microphone permission is only requested when you use the Voice Changer tool. " +
                        "Google AdMob is used to display ads that keep all tools free.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun FeaturedToolCard(tool: ToolItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick)
            .testTag("featured_tool_${tool.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, tool.accentColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(tool.accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = tool.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (tool.isPopular) {
                    Surface(
                        color = Color(0xFFEF4444),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "HOT 🔥",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tool.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tool.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun ToolCard(tool: ToolItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tool.accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = tool.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (tool.isPopular) {
                    Surface(
                        color = tool.accentColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "TOP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = tool.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tool.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = tool.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

private fun openPlayStoreRating(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        )
    } catch (_: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            )
        } catch (_: Exception) {
        }
    }
}

private fun shareApp(context: Context) {
    val packageName = context.packageName
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "OmniTools Pro - 40+ All-in-One Tools")
        putExtra(
            Intent.EXTRA_TEXT,
            "Check out OmniTools Pro! 40+ powerful all-in-one free tools for daily utility, designing, video creation, business & PDF:\nhttps://play.google.com/store/apps/details?id=$packageName"
        )
    }
    try {
        context.startActivity(Intent.createChooser(shareIntent, "Share OmniTools via"))
    } catch (_: Exception) {
    }
}

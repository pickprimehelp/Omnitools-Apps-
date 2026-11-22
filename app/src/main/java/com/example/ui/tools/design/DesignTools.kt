package com.example.ui.tools.design

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.TopBannerAd
import com.example.ads.findActivity
import com.example.util.ImageExportUtils

data class CardDesignTheme(
    val name: String,
    val bgColors: List<Color>,
    val textColor: Color,
    val accentColor: Color,
    val borderColor: Color
)

@Composable
fun WatermarkControlBar(
    hasWatermark: Boolean,
    onWatchAdToSaveClean: () -> Unit
) {
    if (!hasWatermark) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Watermark active on export",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedButton(
                onClick = onWatchAdToSaveClean,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Remove Watermark", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeddingCardMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var brideName by remember { mutableStateOf("Ananya") }
    var groomName by remember { mutableStateOf("Rohan") }
    var dateText by remember { mutableStateOf("14 Dec 2026") }
    var timeText by remember { mutableStateOf("7:30 PM Onwards") }
    var venueText by remember { mutableStateOf("Royal Heritage Palace, Jaipur") }
    var rsvpText by remember { mutableStateOf("Sharma & Verma Family") }
    var themeIndex by remember { mutableIntStateOf(0) }
    var hasWatermark by remember { mutableStateOf(true) }

    val themes = remember {
        listOf(
            CardDesignTheme("Royal Maroon", listOf(Color(0xFF7F1D1D), Color(0xFF450A0A)), Color(0xFFFEF3C7), Color(0xFFF59E0B), Color(0xFFD97706)),
            CardDesignTheme("Emerald Gold", listOf(Color(0xFF064E3B), Color(0xFF022C22)), Color(0xFFECFDF5), Color(0xFFFBBF24), Color(0xFF10B981)),
            CardDesignTheme("Ivory Classic", listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)), Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706)),
            CardDesignTheme("Indigo Night", listOf(Color(0xFF1E1B4B), Color(0xFF312E81)), Color.White, Color(0xFFF472B6), Color(0xFF818CF8))
        )
    }
    val theme = themes[themeIndex]

    fun buildBitmap(): Bitmap {
        val w = 1080
        val h = 1440
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                theme.bgColors.first().toArgb(),
                theme.bgColors.last().toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.borderColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawRoundRect(RectF(36f, 36f, w - 36f, h - 36f), 32f, 32f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentColor.toArgb()
            textSize = 42f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("॥ श्री गणेशाय नमः ॥", w / 2f, 280f, titlePaint)

        val couplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textColor.toArgb()
            textSize = 72f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.SERIF
        }
        canvas.drawText("$groomName  ♥  $brideName", w / 2f, 660f, couplePaint)

        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textColor.toArgb()
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🗓  $dateText", w / 2f, 820f, detailPaint)
        canvas.drawText("⏰  $timeText", w / 2f, 890f, detailPaint)
        canvas.drawText("📍  $venueText", w / 2f, 975f, detailPaint)
        canvas.drawText("RSVP: $rsvpText", w / 2f, 1075f, detailPaint)

        if (hasWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.textColor.copy(alpha = 0.65f).toArgb()
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("⚡ Created with OmniTools App", w / 2f, h - 52f, wmPaint)
        }
        return bmp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wedding Card Maker", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(theme.bgColors))
                        .border(3.dp, theme.borderColor, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("॥ श्री गणेशाय नमः ॥", color = theme.accentColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SHUBH VIVAH INVITATION", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$groomName  ♥  $brideName",
                                color = theme.textColor,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🗓 $dateText  •  ⏰ $timeText", color = theme.textColor, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Text("📍 $venueText", color = theme.textColor, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Text("RSVP: $rsvpText", color = theme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(themes) { idx, t ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = t.bgColors.first(),
                            border = BorderStroke(if (idx == themeIndex) 3.dp else 1.dp, t.borderColor),
                            modifier = Modifier.clickable { themeIndex = idx }
                        ) {
                            Text(
                                t.name,
                                color = t.textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = groomName, onValueChange = { groomName = it }, label = { Text("Groom") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = brideName, onValueChange = { brideName = it }, label = { Text("Bride") }, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = timeText, onValueChange = { timeText = it }, label = { Text("Time") }, modifier = Modifier.weight(1f))
                }
            }
            item {
                OutlinedTextField(value = venueText, onValueChange = { venueText = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = rsvpText, onValueChange = { rsvpText = it }, label = { Text("RSVP / Family") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                WatermarkControlBar(hasWatermark = hasWatermark) {
                    if (activity != null) {
                        AdManager.showRewardedAd(activity, onRewardEarned = { hasWatermark = false }, onDismissed = {})
                    } else {
                        hasWatermark = false
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { ImageExportUtils.saveBitmapToGallery(context, buildBitmap(), "WeddingCard") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save HD")
                    }
                    OutlinedButton(
                        onClick = { ImageExportUtils.shareBitmap(context, buildBitmap(), "Wedding Card") },
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

@Composable
fun StatusMakerScreen(onBack: () -> Unit) {
    GenericCardStudioScreen(
        title = "Status Maker",
        defaultHeadline = "Dream Big. Work Hard. Stay Humble.",
        defaultSubtitle = "Daily Motivation",
        exportPrefix = "OmniTools_Status",
        aspectRatio = 1f,
        onBack = onBack
    )
}

@Composable
fun InvitationCardMakerScreen(onBack: () -> Unit) {
    GenericCardStudioScreen(
        title = "Invitation Card Maker",
        defaultHeadline = "You're Invited to Our Celebration!",
        defaultSubtitle = "Saturday, 7:00 PM • Grand Hall",
        exportPrefix = "OmniTools_Invite",
        aspectRatio = 0.8f,
        onBack = onBack
    )
}

@Composable
fun ReelsMakerScreen(onBack: () -> Unit) {
    GenericCardStudioScreen(
        title = "Reels Cover & Story Maker",
        defaultHeadline = "5 AI & Tech Secrets Nobody Tells You 🚀",
        defaultSubtitle = "@omnitools_creator",
        exportPrefix = "OmniTools_Reel",
        aspectRatio = 9f / 16f,
        onBack = onBack
    )
}

@Composable
fun YouTubeThumbnailMakerScreen(onBack: () -> Unit) {
    GenericCardStudioScreen(
        title = "YouTube Thumbnail Maker",
        defaultHeadline = "HOW TO GROW FAST IN 2026!",
        defaultSubtitle = "STEP BY STEP GUIDE 🔥",
        exportPrefix = "OmniTools_Thumbnail",
        aspectRatio = 16f / 9f,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenericCardStudioScreen(
    title: String,
    defaultHeadline: String,
    defaultSubtitle: String,
    exportPrefix: String,
    aspectRatio: Float,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var headline by remember { mutableStateOf(defaultHeadline) }
    var subtitle by remember { mutableStateOf(defaultSubtitle) }
    var paletteIndex by remember { mutableIntStateOf(0) }
    var hasWatermark by remember { mutableStateOf(true) }

    val palettes = remember {
        listOf(
            Color(0xFF4F46E5) to Color(0xFFEC4899),
            Color(0xFF0F172A) to Color(0xFF334155),
            Color(0xFFDC2626) to Color(0xFFF59E0B),
            Color(0xFF059669) to Color(0xFF0284C7)
        )
    }
    val (c1, c2) = palettes[paletteIndex]

    fun buildExportBitmap(): Bitmap {
        val w = if (aspectRatio >= 1.5f) 1280 else 1080
        val h = (w / aspectRatio).toInt().coerceIn(720, 1920)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), c1.toArgb(), c2.toArgb(), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 56f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(headline, w / 2f, h * 0.45f, headPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(220, 255, 255, 255)
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, w / 2f, h * 0.58f, subPaint)

        if (hasWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(180, 255, 255, 255)
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("⚡ Made with OmniTools App", w / 2f, h - 36f, wmPaint)
        }
        return bmp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.coerceIn(0.65f, 1.8f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(c1, c2)))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = headline,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    palettes.forEachIndexed { idx, pair ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(pair.first, pair.second)))
                                .border(
                                    width = if (idx == paletteIndex) 3.dp else 1.dp,
                                    color = if (idx == paletteIndex) MaterialTheme.colorScheme.primary else Color.White,
                                    shape = CircleShape
                                )
                                .clickable { paletteIndex = idx }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(value = headline, onValueChange = { headline = it }, label = { Text("Main Headline") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle / Footer") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                WatermarkControlBar(hasWatermark = hasWatermark) {
                    if (activity != null) {
                        AdManager.showRewardedAd(activity, onRewardEarned = { hasWatermark = false }, onDismissed = {})
                    } else {
                        hasWatermark = false
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { ImageExportUtils.saveBitmapToGallery(context, buildExportBitmap(), exportPrefix) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save HD")
                    }
                    OutlinedButton(
                        onClick = { ImageExportUtils.shareBitmap(context, buildExportBitmap(), title) },
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

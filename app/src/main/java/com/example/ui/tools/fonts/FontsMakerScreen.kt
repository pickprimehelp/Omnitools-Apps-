package com.example.ui.tools.fonts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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

data class FontVariant(
    val id: String,
    val name: String,
    val sampleText: String,
    val transform: (String) -> String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontsMakerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var inputText by remember { mutableStateOf("OmniTools VIP") }
    var selectedPaletteIndex by remember { mutableIntStateOf(0) }
    var selectedVariantIndex by remember { mutableIntStateOf(0) }
    var fontSize by remember { mutableFloatStateOf(32f) }
    var watermarkRemoved by remember { mutableStateOf(false) }

    val palettes = remember {
        listOf(
            Color(0xFF4F46E5) to Color(0xFFEC4899),
            Color(0xFF0F172A) to Color(0xFF1E293B),
            Color(0xFF059669) to Color(0xFF0284C7),
            Color(0xFFF59E0B) to Color(0xFFEF4444),
            Color(0xFF7C3AED) to Color(0xFF2DD4BF)
        )
    }

    val fontVariants = remember { buildFontVariants() }
    val activeVariant = fontVariants.getOrElse(selectedVariantIndex) { fontVariants.first() }
    val styledPreview = activeVariant.transform(inputText.ifBlank { "OmniTools VIP" })
    val (gradStart, gradEnd) = palettes[selectedPaletteIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fonts Maker & Text Art", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(gradStart, gradEnd)))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = styledPreview,
                        color = Color.White,
                        fontSize = fontSize.coerceIn(18f, 44f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (!watermarkRemoved) {
                        Text(
                            text = "⚡ Made with OmniTools App",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Enter text for fancy fonts & text art") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fonts_text_input")
                )
            }

            item {
                Column {
                    Text("Card Gradient Theme", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(palettes) { index, pair ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(pair.first, pair.second)))
                                    .border(
                                        width = if (index == selectedPaletteIndex) 3.dp else 1.dp,
                                        color = if (index == selectedPaletteIndex) MaterialTheme.colorScheme.primary else Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedPaletteIndex = index }
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Text("Text Size: ${fontSize.toInt()} px", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 20f..56f
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val bmp = renderFontArtBitmap(
                                text = styledPreview,
                                startColor = gradStart.toArgb(),
                                endColor = gradEnd.toArgb(),
                                fontSize = fontSize,
                                includeWatermark = !watermarkRemoved
                            )
                            ImageExportUtils.saveBitmapToGallery(context, bmp, "OmniTools_FontArt")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Art")
                    }
                    OutlinedButton(
                        onClick = {
                            val bmp = renderFontArtBitmap(
                                text = styledPreview,
                                startColor = gradStart.toArgb(),
                                endColor = gradEnd.toArgb(),
                                fontSize = fontSize,
                                includeWatermark = !watermarkRemoved
                            )
                            ImageExportUtils.shareBitmap(context, bmp, "OmniTools Font Art")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Art")
                    }
                }
            }

            if (!watermarkRemoved && activity != null) {
                item {
                    OutlinedButton(
                        onClick = {
                            AdManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { watermarkRemoved = true },
                                onDismissed = {}
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Watch Short Ad to Remove Watermark")
                    }
                }
            }

            item {
                Text("23+ Copyable Unicode Styles", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            itemsIndexed(fontVariants) { index, variant ->
                val transformed = variant.transform(inputText.ifBlank { variant.sampleText })
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedVariantIndex = index },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == selectedVariantIndex) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = variant.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = transformed, fontSize = 16.sp)
                        }
                        IconButton(onClick = {
                            ImageExportUtils.copyToClipboard(context, transformed, variant.name)
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy ${variant.name}")
                        }
                    }
                }
            }
        }
    }
}

private fun renderFontArtBitmap(
    text: String,
    startColor: Int,
    endColor: Int,
    fontSize: Float,
    includeWatermark: Boolean
): Bitmap {
    val width = 1080
    val height = 900
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = (fontSize * 2.3f).coerceIn(40f, 110f)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    canvas.drawText(text, width / 2f, height / 2f, textPaint)

    if (includeWatermark) {
        val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(180, 255, 255, 255)
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("⚡ Made with OmniTools App", width / 2f, height - 36f, wmPaint)
    }
    return bitmap
}

private fun mapAscii(
    text: String,
    upperBase: Int,
    lowerBase: Int,
    digitBase: Int? = null
): String {
    val sb = StringBuilder()
    for (ch in text) {
        when (ch) {
            in 'A'..'Z' -> sb.append(Character.toChars(upperBase + (ch - 'A')))
            in 'a'..'z' -> sb.append(Character.toChars(lowerBase + (ch - 'a')))
            in '0'..'9' -> if (digitBase != null) {
                sb.append(Character.toChars(digitBase + (ch - '0')))
            } else {
                sb.append(ch)
            }
            else -> sb.append(ch)
        }
    }
    return sb.toString()
}

private fun buildFontVariants(): List<FontVariant> = listOf(
    FontVariant("bold", "Bold Serif", "OmniTools") { mapAscii(it, 0x1D400, 0x1D41A, 0x1D7CE) },
    FontVariant("italic", "Italic Serif", "OmniTools") { mapAscii(it, 0x1D434, 0x1D44E) },
    FontVariant("bold_italic", "Bold Italic", "OmniTools") { mapAscii(it, 0x1D468, 0x1D482) },
    FontVariant("sans_bold", "Sans Bold", "OmniTools") { mapAscii(it, 0x1D5D4, 0x1D5EE, 0x1D7EC) },
    FontVariant("sans_italic", "Sans Italic", "OmniTools") { mapAscii(it, 0x1D608, 0x1D622) },
    FontVariant("sans_bold_italic", "Sans Bold Italic", "OmniTools") { mapAscii(it, 0x1D63C, 0x1D656) },
    FontVariant("script", "Cursive Script", "OmniTools") { mapAscii(it, 0x1D4D0, 0x1D4EA) },
    FontVariant("fraktur", "Gothic Bold", "OmniTools") { mapAscii(it, 0x1D56C, 0x1D586) },
    FontVariant("double_struck", "Double Struck", "OmniTools") { mapAscii(it, 0x1D538, 0x1D552, 0x1D7D8) },
    FontVariant("monospace", "Typewriter Mono", "OmniTools") { mapAscii(it, 0x1D670, 0x1D68A, 0x1D7F6) },
    FontVariant("circled", "Circled Bubbles", "OmniTools") { mapAscii(it, 0x24B6, 0x24D0) },
    FontVariant("squared", "Squared Blocks", "OmniTools") { s ->
        mapAscii(s.uppercase(), 0x1F130, 0x1F130)
    },
    FontVariant("filled_squared", "Dark Blocks", "OmniTools") { s ->
        mapAscii(s.uppercase(), 0x1F170, 0x1F170)
    },
    FontVariant("fullwidth", "Vaporwave Wide", "OmniTools") { s ->
        s.map { ch ->
            if (ch in '!'..'~') (ch.code + 0xFEE0).toChar() else if (ch == ' ') '\u3000' else ch
        }.joinToString("")
    },
    FontVariant("small_caps", "Small Caps", "OmniTools") { s ->
        val small = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ"
        s.map { ch ->
            val lower = ch.lowercaseChar()
            if (lower in 'a'..'z') small[lower - 'a'] else ch
        }.joinToString("")
    },
    FontVariant("sparkles", "Sparkle Frame", "OmniTools") { "✨ $it ✨" },
    FontVariant("royal", "Royal Crown", "OmniTools") { "👑 ━━ $it ━━ 👑" },
    FontVariant("brackets", "Aesthetic Brackets", "OmniTools") { "『 $it 』" },
    FontVariant("stars", "Starlight", "OmniTools") { "★彡 $it 彡★" },
    FontVariant("hearts", "Love Frame", "OmniTools") { "♥•٠·˙ $it ˙·٠•♥" },
    FontVariant("spaced", "Spaced Out", "OmniTools") { it.toCharArray().joinToString(" ") },
    FontVariant("underline", "Underlined", "OmniTools") { it.map { c -> "${c}\u0332" }.joinToString("") },
    FontVariant("strike", "Strikethrough", "OmniTools") { it.map { c -> "${c}\u0336" }.joinToString("") }
)

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.example.ads.AdManager
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tools.business.EmiCalculatorScreen
import com.example.ui.tools.business.GstCalculatorScreen
import com.example.ui.tools.business.InvoiceBillMakerScreen
import com.example.ui.tools.business.ProfitDiscountCalculatorScreen
import com.example.ui.tools.creator.HashtagAndTagGeneratorScreen
import com.example.ui.tools.creator.VideoFileSizeCalculatorScreen
import com.example.ui.tools.creator.YouTubeRevenueCalculatorScreen
import com.example.ui.tools.creator.YouTubeTitleGeneratorScreen
import com.example.ui.tools.daily.AgeCalculatorScreen
import com.example.ui.tools.daily.BmiCalculatorScreen
import com.example.ui.tools.daily.QrCodeGeneratorScreen
import com.example.ui.tools.daily.StopwatchAndTimerScreen
import com.example.ui.tools.daily.UnitConverterScreen
import com.example.ui.tools.design.InvitationCardMakerScreen
import com.example.ui.tools.design.ReelsMakerScreen
import com.example.ui.tools.design.StatusMakerScreen
import com.example.ui.tools.design.WeddingCardMakerScreen
import com.example.ui.tools.design.YouTubeThumbnailMakerScreen
import com.example.ui.tools.fonts.FontsMakerScreen
import com.example.ui.tools.image.ImageCompressorScreen
import com.example.ui.tools.image.ImageQualityCheckerScreen
import com.example.ui.tools.image.PassportPhotoMakerScreen
import com.example.ui.tools.pdf.CaseConverterScreen
import com.example.ui.tools.pdf.PhotoToPdfScreen
import com.example.ui.tools.pdf.TextToPdfScreen
import com.example.ui.tools.pdf.WordAndCharacterCounterScreen
import com.example.ui.tools.voice.VoiceChangerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(applicationContext)
        setContent {
            MyApplicationTheme {
                OmniToolsApp()
            }
        }
    }
}

@Composable
fun OmniToolsApp() {
    var activeToolId by remember { mutableStateOf<String?>(null) }
    val goBack: () -> Unit = { activeToolId = null }

    when (activeToolId) {
        null -> HomeScreen(onSelectTool = { toolId -> activeToolId = toolId })

        // Design & Cards
        "wedding_card" -> WeddingCardMakerScreen(onBack = goBack)
        "invitation_card" -> InvitationCardMakerScreen(onBack = goBack)
        "status_maker" -> StatusMakerScreen(onBack = goBack)
        "reels_maker" -> ReelsMakerScreen(onBack = goBack)
        "yt_thumbnail" -> YouTubeThumbnailMakerScreen(onBack = goBack)
        "fonts_maker" -> FontsMakerScreen(onBack = goBack)

        // Creator Tools
        "yt_title_gen", "yt_desc_gen", "ig_caption_gen" ->
            YouTubeTitleGeneratorScreen(isShortsMode = false, onBack = goBack)
        "shorts_title_gen" ->
            YouTubeTitleGeneratorScreen(isShortsMode = true, onBack = goBack)
        "hashtag_gen" ->
            HashtagAndTagGeneratorScreen(isTagMode = false, onBack = goBack)
        "tag_gen" ->
            HashtagAndTagGeneratorScreen(isTagMode = true, onBack = goBack)
        "yt_revenue_calc", "cpm_rpm_calc" ->
            YouTubeRevenueCalculatorScreen(onBack = goBack)
        "thumbnail_size", "video_file_size" ->
            VideoFileSizeCalculatorScreen(onBack = goBack)

        // Business & Finance
        "invoice_maker" -> InvoiceBillMakerScreen(onBack = goBack)
        "gst_calc", "tax_calc" -> GstCalculatorScreen(onBack = goBack)
        "emi_calc", "loan_calc", "salary_calc" -> EmiCalculatorScreen(onBack = goBack)
        "profit_margin", "discount_calc", "percentage_calc", "tip_calc" ->
            ProfitDiscountCalculatorScreen(onBack = goBack)

        // Image Tools
        "image_compressor", "image_resizer", "format_converter" ->
            ImageCompressorScreen(onBack = goBack)
        "passport_photo", "photo_size_calc" ->
            PassportPhotoMakerScreen(onBack = goBack)
        "image_cropper", "blur_image", "image_quality" ->
            ImageQualityCheckerScreen(onBack = goBack)

        // PDF & Document
        "image_to_pdf_quick", "pdf_merger" -> PhotoToPdfScreen(onBack = goBack)
        "text_to_pdf" -> TextToPdfScreen(onBack = goBack)
        "word_counter", "character_counter" -> WordAndCharacterCounterScreen(onBack = goBack)
        "case_converter" -> CaseConverterScreen(onBack = goBack)

        // Voice & Audio
        "voice_changer" -> VoiceChangerScreen(onBack = goBack)

        // Daily Utilities
        "qr_generator", "barcode_generator" -> QrCodeGeneratorScreen(onBack = goBack)
        "age_calculator", "date_diff_calc" -> AgeCalculatorScreen(onBack = goBack)
        "bmi_calculator" -> BmiCalculatorScreen(onBack = goBack)
        NotificationCompat.CATEGORY_STOPWATCH, "countdown_timer" ->
            StopwatchAndTimerScreen(onBack = goBack)
        "unit_converter", "currency_converter", "timezone_converter" ->
            UnitConverterScreen(onBack = goBack)

        else -> HomeScreen(onSelectTool = { toolId -> activeToolId = toolId })
    }
}

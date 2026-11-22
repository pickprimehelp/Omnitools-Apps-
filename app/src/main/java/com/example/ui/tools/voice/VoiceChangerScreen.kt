package com.example.ui.tools.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ads.TopBannerAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class VoiceFilter(
    val name: String,
    val emoji: String,
    val pitch: Float,
    val speed: Float,
    val description: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChangerScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sampleRate = 22050
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }
    var recordedPcm by remember { mutableStateOf<ShortArray?>(null) }

    val filters = remember {
        listOf(
            VoiceFilter("Normal", "🎙️", 1.0f, 1.0f, "Natural studio voice", Color(0xFF3B82F6)),
            VoiceFilter("Chipmunk", "🐿️", 1.55f, 1.15f, "High-pitched funny cartoon", Color(0xFFF59E0B)),
            VoiceFilter("Helium", "🎈", 1.80f, 1.05f, "Ultra light helium balloon", Color(0xFFEC4899)),
            VoiceFilter("Deep Monster", "👹", 0.65f, 0.90f, "Heavy cinematic villain", Color(0xFFEF4444)),
            VoiceFilter("Robot", "🤖", 0.92f, 1.0f, "Metallic sci-fi modulator", Color(0xFF06B6D4)),
            VoiceFilter("Slow-Mo", "🐢", 0.80f, 0.72f, "Dramatic slow-motion effect", Color(0xFF8B5CF6))
        )
    }
    var selectedFilter by remember { mutableStateOf(filters.first()) }

    val activeRecorder = remember { mutableStateOf<AudioRecord?>(null) }
    val activeTrack = remember { mutableStateOf<AudioTrack?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                activeRecorder.value?.stop()
                activeRecorder.value?.release()
            } catch (_: Throwable) {
            }
            try {
                activeTrack.value?.stop()
                activeTrack.value?.release()
            } catch (_: Throwable) {
            }
        }
    }

    fun startRecording() {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf
            )
        } catch (e: SecurityException) {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to access microphone: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            Toast.makeText(context, "Microphone unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        activeRecorder.value = recorder
        isRecording = true
        recordSeconds = 0

        scope.launch(Dispatchers.IO) {
            val out = ByteArrayOutputStream()
            val buf = ShortArray(minBuf / 2)
            try {
                recorder.startRecording()
                val startMs = System.currentTimeMillis()
                while (isActive && isRecording) {
                    val read = recorder.read(buf, 0, buf.size)
                    if (read > 0) {
                        val bb = ByteBuffer.allocate(read * 2).order(ByteOrder.LITTLE_ENDIAN)
                        for (i in 0 until read) bb.putShort(buf[i])
                        out.write(bb.array())
                    }
                    val secs = ((System.currentTimeMillis() - startMs) / 1000L).toInt()
                    withContext(Dispatchers.Main) { recordSeconds = secs }
                }
                recorder.stop()
            } catch (_: Exception) {
            } finally {
                recorder.release()
                activeRecorder.value = null
            }

            val bytes = out.toByteArray()
            val shorts = ShortArray(bytes.size / 2)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            withContext(Dispatchers.Main) {
                recordedPcm = shorts
                isRecording = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            startRecording()
        } else {
            Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun playFilteredVoice() {
        val source = recordedPcm ?: return
        if (source.isEmpty()) return
        isPlaying = true

        scope.launch(Dispatchers.IO) {
            val processed = applyVoiceFilter(source, selectedFilter)
            val playbackRate = (sampleRate * selectedFilter.pitch).toInt().coerceIn(8000, 48000)
            val bufBytes = (processed.size * 2).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(playbackRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            activeTrack.value = track
            try {
                track.write(processed, 0, processed.size)
                track.play()
                val durationMs = ((processed.size.toDouble() / playbackRate) * 1000.0).toLong().coerceAtLeast(300L)
                delay(durationMs + 150L)
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Throwable) {
                }
                activeTrack.value = null
                withContext(Dispatchers.Main) { isPlaying = false }
            }
        }
    }

    fun shareFilteredWav() {
        val source = recordedPcm ?: return
        if (source.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            try {
                val processed = applyVoiceFilter(source, selectedFilter)
                val playbackRate = (sampleRate * selectedFilter.pitch).toInt().coerceIn(8000, 48000)
                val cacheDir = File(context.cacheDir, "audio").apply { mkdirs() }
                val audioFile = File(cacheDir, "omnitools_${selectedFilter.name.lowercase()}.wav")
                writeWavFile(audioFile, processed, playbackRate)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", audioFile)
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/wav"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Voice Clip"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Changer Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                                )
                                .clickable {
                                    if (isRecording) {
                                        isRecording = false
                                    } else if (hasMicPermission) {
                                        startRecording()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                .testTag("record_voice_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Record Voice",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when {
                                isRecording -> "Recording... ${recordSeconds}s (Tap to Stop)"
                                recordedPcm != null -> "Voice Ready! Pick a filter below"
                                else -> "Tap Mic to Record Your Voice"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (recordedPcm != null && !isRecording) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { playFilteredVoice() },
                                    enabled = !isPlaying,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = selectedFilter.color)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isPlaying) "Playing..." else "Play ${selectedFilter.name}")
                                }
                                OutlinedButton(
                                    onClick = { shareFilteredWav() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share WAV")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Select Voice Effect", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(filters) { filter ->
                val isSelected = selectedFilter.name == filter.name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFilter = filter },
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(2.dp, filter.color) else null,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) filter.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(filter.emoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(filter.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(filter.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "${filter.pitch}x",
                            fontWeight = FontWeight.Bold,
                            color = filter.color,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun applyVoiceFilter(input: ShortArray, filter: VoiceFilter): ShortArray {
    if (filter.name != "Robot") return input
    val output = ShortArray(input.size)
    for (i in input.indices) {
        val mod = kotlin.math.sin(2.0 * Math.PI * 55.0 * i / 22050.0)
        output[i] = (input[i] * mod).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return output
}

private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
    val dataSize = pcm.size * 2
    val totalSize = 36 + dataSize
    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
        put("RIFF".toByteArray())
        putInt(totalSize)
        put("WAVE".toByteArray())
        put("fmt ".toByteArray())
        putInt(16)
        putShort(1)
        putShort(1)
        putInt(sampleRate)
        putInt(sampleRate * 2)
        putShort(2)
        putShort(16)
        put("data".toByteArray())
        putInt(dataSize)
    }
    FileOutputStream(file).use { out ->
        out.write(header.array())
        val dataBuf = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (s in pcm) dataBuf.putShort(s)
        out.write(dataBuf.array())
    }
}

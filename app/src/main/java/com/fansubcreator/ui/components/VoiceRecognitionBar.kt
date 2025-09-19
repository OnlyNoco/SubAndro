package com.fansubcreator.ui.components

import android.Manifest
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.sin
import kotlin.random.Random

data class VoiceSegment(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val amplitude: Float,
    val text: String = ""
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceRecognitionBar(
    currentTimeMs: Long,
    videoDurationMs: Long,
    voiceSegments: List<VoiceSegment>,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSegmentSelected: (VoiceSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    var currentAmplitude by remember { mutableStateOf(0f) }
    
    // Simulate voice amplitude during recording (replace with actual audio capture)
    LaunchedEffect(isRecording) {
        while (isRecording) {
            currentAmplitude = Random.nextFloat() * 100f
            kotlinx.coroutines.delay(100)
        }
        if (!isRecording) {
            currentAmplitude = 0f
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Voice Recognition",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Recording controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (audioPermission.status.isGranted) {
                        IconButton(
                            onClick = if (isRecording) onStopRecording else onStartRecording,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                tint = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = { audioPermission.launchPermissionRequest() }
                        ) {
                            Icon(Icons.Default.MicOff, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Allow Microphone")
                        }
                    }
                }
            }
            
            // Voice waveform visualization
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.1f)
                )
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawVoiceWaveform(
                        currentTimeMs = currentTimeMs,
                        videoDurationMs = videoDurationMs,
                        voiceSegments = voiceSegments,
                        currentAmplitude = if (isRecording) currentAmplitude else 0f,
                        isRecording = isRecording
                    )
                }
            }
            
            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Auto-detect voice segments */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Auto Detect")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { /* Clear all segments */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            }
            
            // Voice segments list
            if (voiceSegments.isNotEmpty()) {
                Text(
                    text = "Detected Segments (${voiceSegments.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    voiceSegments.take(3).forEach { segment ->
                        Card(
                            onClick = { onSegmentSelected(segment) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = formatTime(segment.startTimeMs) + " - " + formatTime(segment.endTimeMs),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (segment.text.isNotEmpty()) {
                                        Text(
                                            text = segment.text,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                // Amplitude indicator
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            when {
                                                segment.amplitude > 70 -> Color.Red
                                                segment.amplitude > 40 -> Color.Yellow
                                                else -> Color.Green
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                    
                    if (voiceSegments.size > 3) {
                        Text(
                            text = "... and ${voiceSegments.size - 3} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawVoiceWaveform(
    currentTimeMs: Long,
    videoDurationMs: Long,
    voiceSegments: List<VoiceSegment>,
    currentAmplitude: Float,
    isRecording: Boolean
) {
    val width = size.width
    val height = size.height
    val centerY = height / 2
    
    // Draw timeline background
    drawLine(
        color = Color.Gray,
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 2f
    )
    
    // Draw current position indicator
    if (videoDurationMs > 0) {
        val currentX = (currentTimeMs.toFloat() / videoDurationMs) * width
        drawLine(
            color = Color.Red,
            start = Offset(currentX, 0f),
            end = Offset(currentX, height),
            strokeWidth = 3f
        )
    }
    
    // Draw voice segments
    voiceSegments.forEach { segment ->
        if (videoDurationMs > 0) {
            val startX = (segment.startTimeMs.toFloat() / videoDurationMs) * width
            val endX = (segment.endTimeMs.toFloat() / videoDurationMs) * width
            val segmentWidth = endX - startX
            
            // Draw segment background
            drawRect(
                color = Color.Blue.copy(alpha = 0.3f),
                topLeft = Offset(startX, centerY - 20),
                size = androidx.compose.ui.geometry.Size(segmentWidth, 40f)
            )
            
            // Draw amplitude waveform for segment
            val amplitudeHeight = (segment.amplitude / 100f) * (height / 2 - 10)
            drawLine(
                color = Color.Blue,
                start = Offset(startX + segmentWidth/2, centerY - amplitudeHeight),
                end = Offset(startX + segmentWidth/2, centerY + amplitudeHeight),
                strokeWidth = 2f
            )
        }
    }
    
    // Draw live recording waveform
    if (isRecording && videoDurationMs > 0) {
        val currentX = (currentTimeMs.toFloat() / videoDurationMs) * width
        val amplitudeHeight = (currentAmplitude / 100f) * (height / 2 - 10)
        
        // Animated waveform
        for (i in 0..20) {
            val x = currentX + i * 3f
            if (x < width) {
                val waveHeight = amplitudeHeight * sin(i * 0.3f + currentTimeMs * 0.01f)
                drawLine(
                    color = Color.Red,
                    start = Offset(x, centerY - waveHeight),
                    end = Offset(x, centerY + waveHeight),
                    strokeWidth = 1.5f
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
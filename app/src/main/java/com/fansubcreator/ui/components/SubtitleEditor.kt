package com.fansubcreator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fansubcreator.R
import com.fansubcreator.data.SubtitleCue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleEditor(
    subtitle: SubtitleCue?,
    onDismiss: () -> Unit,
    onTextChanged: (String) -> Unit,
    onTimingChanged: (Long, Long) -> Unit,
    onStyleChanged: (fontSize: Float?, fontColor: Color?, backgroundColor: Color?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (subtitle == null) return
    
    var text by remember(subtitle.id) { mutableStateOf(subtitle.text) }
    var startTime by remember(subtitle.id) { mutableStateOf(subtitle.startTimeMs) }
    var endTime by remember(subtitle.id) { mutableStateOf(subtitle.endTimeMs) }
    var fontSize by remember(subtitle.id) { mutableStateOf(subtitle.fontSize.value) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Subtitle",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                
                // Text editing
                OutlinedTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        onTextChanged(it)
                    },
                    label = { Text(stringResource(R.string.subtitle_text)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                // Timing controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formatTime(startTime),
                        onValueChange = { input ->
                            parseTime(input)?.let { parsedTime ->
                                startTime = parsedTime
                            }
                        },
                        label = { Text(stringResource(R.string.start_time)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = formatTime(endTime),
                        onValueChange = { input ->
                            parseTime(input)?.let { parsedTime ->
                                if (parsedTime > startTime) {
                                    endTime = parsedTime
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.end_time)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Style controls
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Style",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Font size slider
                    Column {
                        Text("Font Size: ${fontSize.toInt()}sp")
                        Slider(
                            value = fontSize,
                            onValueChange = { 
                                fontSize = it
                                onStyleChanged(it, null, null)
                            },
                            valueRange = 12f..36f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Color buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Font color options
                        listOf(Color.White, Color.Yellow, Color.Red, Color.Blue).forEach { color ->
                            Button(
                                onClick = { onStyleChanged(null, color, null) },
                                colors = ButtonDefaults.buttonColors(containerColor = color),
                                modifier = Modifier.size(40.dp)
                            ) {}
                        }
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onTimingChanged(startTime, endTime)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val seconds = timeMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val milliseconds = (timeMs % 1000) / 10
    return "%02d:%02d.%02d".format(minutes, remainingSeconds, milliseconds)
}

private fun parseTime(timeStr: String): Long? {
    return try {
        val parts = timeStr.split(":")
        if (parts.size != 2) return null
        
        val minutes = parts[0].toLongOrNull() ?: return null
        val secondsParts = parts[1].split(".")
        val seconds = secondsParts[0].toLongOrNull() ?: return null
        val milliseconds = if (secondsParts.size > 1) {
            (secondsParts[1].padEnd(2, '0').take(2).toLongOrNull() ?: 0) * 10
        } else 0
        
        (minutes * 60 + seconds) * 1000 + milliseconds
    } catch (e: Exception) {
        null
    }
}
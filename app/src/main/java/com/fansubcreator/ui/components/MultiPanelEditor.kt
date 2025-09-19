package com.fansubcreator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fansubcreator.data.AssEvent
import com.fansubcreator.data.AssFile
import com.fansubcreator.data.AssStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPanelEditor(
    assFile: AssFile,
    selectedEventId: String?,
    currentTimeMs: Long,
    textClipboard: String,
    onEventSelected: (String) -> Unit,
    onEventUpdated: (AssEvent) -> Unit,
    onEventDeleted: (String) -> Unit,
    onStyleUpdated: (AssStyle) -> Unit,
    onTimingShift: (Long) -> Unit,
    onTextClipboardUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Left Panel: Event List & Timeline
        Card(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(4.dp)
        ) {
            Column {
                // Timeline Controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Timeline Controls",
                            style = MaterialTheme.typography.titleSmall
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onTimingShift(-100) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Shift Back")
                            }
                            
                            Text(
                                text = "-0.1s",
                                modifier = Modifier.align(Alignment.CenterVertically),
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            IconButton(
                                onClick = { onTimingShift(100) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Shift Forward")
                            }
                            
                            Text(
                                text = "+0.1s",
                                modifier = Modifier.align(Alignment.CenterVertically),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onTimingShift(-1000) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("-1s", fontSize = 12.sp)
                            }
                            
                            Button(
                                onClick = { onTimingShift(1000) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("+1s", fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                // Event List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(assFile.events, key = { it.id }) { event ->
                        EventListItem(
                            event = event,
                            isSelected = event.id == selectedEventId,
                            isActive = event.isActiveAt(currentTimeMs),
                            onClick = { onEventSelected(event.id) },
                            onDelete = { onEventDeleted(event.id) }
                        )
                    }
                }
            }
        }
        
        // Center Panel: Text Editor & Clipboard
        Card(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .padding(4.dp)
        ) {
            Column {
                // Text Clipboard Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Text Clipboard",
                                style = MaterialTheme.typography.titleSmall
                            )
                            
                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(textClipboard))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                
                                IconButton(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        onTextClipboardUpdated(clipText)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                                }
                            }
                        }
                        
                        BasicTextField(
                            value = textClipboard,
                            onValueChange = onTextClipboardUpdated,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(8.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            maxLines = 5
                        )
                    }
                }
                
                // Selected Event Editor
                selectedEventId?.let { eventId ->
                    val event = assFile.events.find { it.id == eventId }
                    event?.let { currentEvent ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            EventEditor(
                                event = currentEvent,
                                styles = assFile.styles,
                                onEventUpdated = onEventUpdated,
                                textClipboard = textClipboard
                            )
                        }
                    }
                }
            }
        }
        
        // Right Panel: Style Editor
        Card(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .padding(4.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Styles",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                items(assFile.styles, key = { it.name }) { style ->
                    StyleEditor(
                        style = style,
                        onStyleUpdated = onStyleUpdated
                    )
                }
            }
        }
    }
}

@Composable
private fun EventListItem(
    event: AssEvent,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatEventTime(event.start, event.end),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = event.text.take(30) + if (event.text.length > 30) "..." else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EventEditor(
    event: AssEvent,
    styles: List<AssStyle>,
    onEventUpdated: (AssEvent) -> Unit,
    textClipboard: String
) {
    var text by remember(event.id) { mutableStateOf(event.text) }
    var selectedStyle by remember(event.id) { mutableStateOf(event.style) }
    
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Edit Event",
            style = MaterialTheme.typography.titleSmall
        )
        
        // Text editor
        OutlinedTextField(
            value = text,
            onValueChange = { 
                text = it
                onEventUpdated(event.copy(text = it))
            },
            label = { Text("Subtitle Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        // Quick paste from clipboard
        if (textClipboard.isNotEmpty()) {
            Button(
                onClick = { 
                    text = textClipboard
                    onEventUpdated(event.copy(text = textClipboard))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paste from Clipboard")
            }
        }
        
        // Style selector
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStyle,
                onValueChange = { },
                readOnly = true,
                label = { Text("Style") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                styles.forEach { style ->
                    DropdownMenuItem(
                        text = { Text(style.name) },
                        onClick = {
                            selectedStyle = style.name
                            onEventUpdated(event.copy(style = style.name))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleEditor(
    style: AssStyle,
    onStyleUpdated: (AssStyle) -> Unit
) {
    var fontSize by remember(style.name) { mutableStateOf(style.fontSize.toFloat()) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = style.name,
                style = MaterialTheme.typography.titleSmall
            )
            
            // Font size slider
            Column {
                Text("Font Size: ${fontSize.toInt()}", fontSize = 12.sp)
                Slider(
                    value = fontSize,
                    onValueChange = { 
                        fontSize = it
                        onStyleUpdated(style.copy(fontSize = it.toInt()))
                    },
                    valueRange = 8f..72f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatEventTime(start: Long, end: Long): String {
    return "${formatTime(start)} - ${formatTime(end)}"
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
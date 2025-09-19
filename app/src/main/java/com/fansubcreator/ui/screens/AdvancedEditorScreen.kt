package com.fansubcreator.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.fansubcreator.ui.components.*
import com.fansubcreator.utils.FileManager
import com.fansubcreator.viewmodel.AdvancedSubtitleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedEditorScreen(
    videoUri: Uri,
    onBack: () -> Unit,
    viewModel: AdvancedSubtitleViewModel = viewModel()
) {
    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    var videoSize by remember { mutableStateOf(IntSize.Zero) }
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // Initialize project
    LaunchedEffect(videoUri) {
        viewModel.createProject(videoUri)
    }
    
    // File picker launchers
    val loadProjectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadProject(fileManager, it) }
    }
    
    val saveProjectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(FileManager.ASS_MIME_TYPE)
    ) { uri ->
        uri?.let { viewModel.saveProject(fileManager, it) }
    }
    
    val exportSrtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(FileManager.SRT_MIME_TYPE)
    ) { uri ->
        uri?.let { viewModel.exportToSrt(fileManager, it) }
    }
    
    val loadTextLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadTextClipboard(fileManager, it) }
    }
    
    // Video player
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    
    // Update playback state
    LaunchedEffect(exoPlayer) {
        while (true) {
            viewModel.updatePlaybackState(
                isPlaying = exoPlayer.isPlaying,
                currentTimeMs = exoPlayer.currentPosition,
                durationMs = exoPlayer.duration
            )
            kotlinx.coroutines.delay(100)
        }
    }
    
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top toolbar
        TopAppBar(
            title = { Text("Advanced Fansub Editor") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        IconButton(onClick = { loadProjectLauncher.launch("*/*") }) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Load Project")
                        }
                    }
                    item {
                        IconButton(onClick = { saveProjectLauncher.launch("project.ass") }) {
                            Icon(Icons.Default.Save, contentDescription = "Save Project")
                        }
                    }
                    item {
                        IconButton(onClick = { viewModel.showExportDialog() }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export")
                        }
                    }
                    item {
                        IconButton(onClick = { loadTextLauncher.launch("text/*") }) {
                            Icon(Icons.Default.TextSnippet, contentDescription = "Load Text")
                        }
                    }
                    item {
                        IconButton(onClick = { showBottomSheet = !showBottomSheet }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            }
        )
        
        // Main content
        Row(
            modifier = Modifier.weight(1f)
        ) {
            // Left column: Video and controls
            Column(
                modifier = Modifier.weight(0.6f)
            ) {
                // Video player with overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                        .onSizeChanged { videoSize = it }
                ) {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // ASS subtitle overlay
                    AssSubtitleOverlay(
                        events = viewModel.getActiveEvents(),
                        styles = viewModel.assFile?.styles ?: emptyList(),
                        selectedEventId = viewModel.selectedEventId,
                        videoSize = videoSize,
                        onEventSelected = viewModel::selectEvent,
                        onEventUpdated = viewModel::updateEvent
                    )
                }
                
                // Video controls
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                        ) {
                            Icon(
                                if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (exoPlayer.isPlaying) "Pause" else "Play"
                            )
                        }
                        
                        Button(
                            onClick = { viewModel.addEventAtCurrentTime() }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Sub")
                        }
                        
                        Button(
                            onClick = {
                                if (viewModel.isRecording) {
                                    viewModel.stopRecording()
                                } else {
                                    viewModel.startRecording()
                                }
                            },
                            colors = if (viewModel.isRecording) {
                                ButtonDefaults.buttonColors(containerColor = Color.Red)
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            Icon(
                                if (viewModel.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (viewModel.isRecording) "Stop" else "Record")
                        }
                    }
                }
                
                // Voice recognition bar
                VoiceRecognitionBar(
                    currentTimeMs = viewModel.currentTimeMs,
                    videoDurationMs = viewModel.videoDurationMs,
                    voiceSegments = viewModel.voiceSegments,
                    isRecording = viewModel.isRecording,
                    onStartRecording = viewModel::startRecording,
                    onStopRecording = viewModel::stopRecording,
                    onSegmentSelected = viewModel::selectVoiceSegment,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            // Right column: Multi-panel editor
            viewModel.assFile?.let { assFile ->
                MultiPanelEditor(
                    assFile = assFile,
                    selectedEventId = viewModel.selectedEventId,
                    currentTimeMs = viewModel.currentTimeMs,
                    textClipboard = viewModel.textClipboard,
                    onEventSelected = viewModel::selectEvent,
                    onEventUpdated = viewModel::updateEvent,
                    onEventDeleted = viewModel::deleteEvent,
                    onStyleUpdated = viewModel::updateStyle,
                    onTimingShift = viewModel::shiftAllTiming,
                    onTextClipboardUpdated = viewModel::updateTextClipboard,
                    modifier = Modifier.weight(0.4f)
                )
            }
        }
        
        // Quick timing controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.shiftAllTiming(-5000) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                ) {
                    Text("-5s All")
                }
                
                Button(
                    onClick = { viewModel.shiftSelectedTiming(-1000) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Orange.copy(alpha = 0.7f))
                ) {
                    Text("-1s Sel")
                }
                
                Button(
                    onClick = { viewModel.shiftSelectedTiming(1000) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.7f))
                ) {
                    Text("+1s Sel")
                }
                
                Button(
                    onClick = { viewModel.shiftAllTiming(5000) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.7f))
                ) {
                    Text("+5s All")
                }
            }
        }
    }
    
    // Translation bottom sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            TranslationPanel(
                originalTexts = viewModel.originalTexts,
                translatedTexts = viewModel.translatedTexts,
                onTranslatedTextsUpdated = viewModel::updateTranslatedTexts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(16.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        viewModel.applyTranslationsToSubtitles()
                        showBottomSheet = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Translations")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Export dialog
    if (viewModel.showExportDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideExportDialog,
            title = { Text("Export Subtitles") },
            text = { Text("Choose export format:") },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            saveProjectLauncher.launch("subtitles.ass")
                            viewModel.hideExportDialog()
                        }
                    ) {
                        Text("ASS")
                    }
                    
                    TextButton(
                        onClick = {
                            exportSrtLauncher.launch("subtitles.srt")
                            viewModel.hideExportDialog()
                        }
                    ) {
                        Text("SRT")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideExportDialog) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Error snackbar
    viewModel.errorMessage?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}

// ASS subtitle overlay component with full styling support
@Composable
private fun AssSubtitleOverlay(
    events: List<com.fansubcreator.data.AssEvent>,
    styles: List<com.fansubcreator.data.AssStyle>,
    selectedEventId: String?,
    videoSize: IntSize,
    onEventSelected: (String) -> Unit,
    onEventUpdated: (com.fansubcreator.data.AssEvent) -> Unit
) {
    // This would render ASS subtitles with full styling support
    // For now, showing a simplified version
    Box(modifier = Modifier.fillMaxSize()) {
        events.forEach { event ->
            val style = styles.find { it.name == event.style } ?: styles.firstOrNull()
            
            Card(
                onClick = { onEventSelected(event.id) },
                modifier = Modifier.align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = style?.primaryColor?.copy(alpha = 0.8f) ?: Color.Black.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = event.text,
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }
        }
    }
}
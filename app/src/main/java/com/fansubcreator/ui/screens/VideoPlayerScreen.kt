package com.fansubcreator.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.fansubcreator.R
import com.fansubcreator.ui.components.DraggableSubtitleOverlay
import com.fansubcreator.ui.components.SubtitleEditor
import com.fansubcreator.viewmodel.SubtitleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    onBack: () -> Unit,
    viewModel: SubtitleViewModel = viewModel()
) {
    val context = LocalContext.current
    var videoSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Initialize project
    LaunchedEffect(videoUri) {
        viewModel.createProject(videoUri, "New Project")
    }
    
    val project = viewModel.project
    val activeSubtitles = viewModel.getActiveSubtitles()
    val selectedSubtitle = project?.subtitles?.find { it.id == viewModel.selectedSubtitleId }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            
            // Player listener for tracking playback
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    viewModel.updatePlaybackState(
                        isPlaying = playbackState == Player.STATE_READY && this@apply.isPlaying,
                        currentTimeMs = this@apply.currentPosition
                    )
                }
            })
        }
    }
    
    // Update current time regularly
    LaunchedEffect(exoPlayer) {
        while (true) {
            viewModel.updatePlaybackState(
                isPlaying = exoPlayer.isPlaying,
                currentTimeMs = exoPlayer.currentPosition
            )
            kotlinx.coroutines.delay(100) // Update every 100ms
        }
    }
    
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Fansub Editor") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Export subtitles */ }) {
                    Text("Export")
                }
            }
        )
        
        // Video Player with Subtitle Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .background(Color.Black)
                .onSizeChanged { videoSize = it }
        ) {
            // Video Player
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = false // Use custom controls
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Draggable Subtitle Overlay
            DraggableSubtitleOverlay(
                subtitles = activeSubtitles,
                selectedSubtitleId = viewModel.selectedSubtitleId,
                videoSize = videoSize,
                onSubtitlePositionChanged = { id, position ->
                    viewModel.updateSubtitlePosition(id, position)
                },
                onSubtitleSelected = { id ->
                    viewModel.selectSubtitle(id)
                }
            )
        }
        
        // Control Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
            ) {
                Icon(
                    if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (exoPlayer.isPlaying) "Pause" else "Play"
                )
            }
            
            Button(onClick = { 
                viewModel.addSubtitleAtCurrentTime()
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_subtitle))
            }
            
            Button(onClick = { /* TODO: Voice recognition */ }) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.voice_recognition))
            }
        }
        
        // Subtitle Timeline (simplified for now)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Subtitle Timeline",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar showing current position
                LinearProgressIndicator(
                    progress = if (exoPlayer.duration > 0) {
                        (exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()).coerceIn(0f, 1f)
                    } else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${formatTime(exoPlayer.currentPosition)} / ${formatTime(exoPlayer.duration)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Subtitle List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            project?.subtitles?.let { subtitles ->
                items(subtitles, key = { it.id }) { subtitle ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.selectSubtitle(subtitle.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = subtitle.text.take(50) + if (subtitle.text.length > 50) "..." else "",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${formatTime(subtitle.startTimeMs)} --> ${formatTime(subtitle.endTimeMs)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Subtitle Editor Dialog
    if (viewModel.showSubtitleEditor && selectedSubtitle != null) {
        SubtitleEditor(
            subtitle = selectedSubtitle,
            onDismiss = { viewModel.closeSubtitleEditor() },
            onTextChanged = { newText ->
                viewModel.updateSubtitleText(selectedSubtitle.id, newText)
            },
            onTimingChanged = { startMs, endMs ->
                viewModel.updateSubtitleTiming(selectedSubtitle.id, startMs, endMs)
            },
            onStyleChanged = { fontSize, fontColor, backgroundColor ->
                viewModel.updateSubtitleStyle(selectedSubtitle.id, fontSize, fontColor, backgroundColor)
            },
            onDelete = {
                viewModel.deleteSubtitle(selectedSubtitle.id)
            }
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val seconds = timeMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
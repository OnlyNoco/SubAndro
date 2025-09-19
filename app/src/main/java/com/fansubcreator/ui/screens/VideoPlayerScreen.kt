package com.fansubcreator.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.fansubcreator.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
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
        
        // Video Player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Subtitle overlay (placeholder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Sample subtitle text",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(8.dp)
                )
            }
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
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    isPlaying = !isPlaying
                }
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            
            Button(onClick = { /* TODO: Add subtitle at current time */ }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_subtitle))
            }
            
            Button(onClick = { /* TODO: Voice recognition */ }) {
                Text(stringResource(R.string.voice_recognition))
            }
        }
        
        // Subtitle Timeline
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
                
                // Placeholder timeline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.small
                        )
                ) {
                    Text(
                        text = "Timeline will show subtitle segments here",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Subtitle List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(3) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: Edit subtitle */ }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Subtitle ${index + 1}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "00:00:${String.format("%02d", index * 5)} --> 00:00:${String.format("%02d", (index + 1) * 5)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Sample subtitle text for testing",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
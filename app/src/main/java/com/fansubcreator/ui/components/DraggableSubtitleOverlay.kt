package com.fansubcreator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.fansubcreator.data.SubtitleCue
import kotlin.math.roundToInt

@Composable
fun DraggableSubtitleOverlay(
    subtitles: List<SubtitleCue>,
    selectedSubtitleId: String?,
    videoSize: androidx.compose.ui.unit.IntSize,
    onSubtitlePositionChanged: (String, Offset) -> Unit,
    onSubtitleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Box(modifier = modifier.fillMaxSize()) {
        subtitles.forEach { subtitle ->
            val isSelected = subtitle.id == selectedSubtitleId
            var dragOffset by remember { mutableStateOf(Offset.Zero) }
            
            // Convert relative position (0-1) to absolute pixels
            val absolutePosition = with(density) {
                Offset(
                    x = subtitle.position.x * videoSize.width,
                    y = subtitle.position.y * videoSize.height
                )
            }
            
            val finalPosition = absolutePosition + dragOffset
            
            Card(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = finalPosition.x.roundToInt(),
                            y = finalPosition.y.roundToInt()
                        )
                    }
                    .pointerInput(subtitle.id) {
                        detectDragGestures(
                            onDragStart = {
                                onSubtitleSelected(subtitle.id)
                            },
                            onDragEnd = {
                                // Convert back to relative position and save
                                val newRelativePosition = Offset(
                                    x = (finalPosition.x / videoSize.width).coerceIn(0f, 1f),
                                    y = (finalPosition.y / videoSize.height).coerceIn(0f, 1f)
                                )
                                onSubtitlePositionChanged(subtitle.id, newRelativePosition)
                                dragOffset = Offset.Zero
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    }
                    .shadow(
                        elevation = if (isSelected) 8.dp else 4.dp,
                        shape = RoundedCornerShape(4.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        subtitle.backgroundColor.copy(alpha = 0.9f) 
                    else subtitle.backgroundColor
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = subtitle.text,
                    color = subtitle.fontColor,
                    fontSize = subtitle.fontSize,
                    fontWeight = subtitle.fontWeight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (finalPosition.x - 4).roundToInt(),
                                y = (finalPosition.y - 4).roundToInt()
                            )
                        }
                        .size(8.dp)
                        .background(
                            Color.Blue,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
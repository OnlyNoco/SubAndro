package com.fansubcreator.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class SubtitleCue(
    val id: String,
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val position: Offset = Offset.Zero,
    val fontSize: TextUnit = 18.sp,
    val fontColor: Color = Color.White,
    val backgroundColor: Color = Color.Black.copy(alpha = 0.7f),
    val fontWeight: FontWeight = FontWeight.Normal,
    val outlineColor: Color = Color.Black,
    val outlineWidth: Float = 2f
) {
    val durationMs: Long get() = endTimeMs - startTimeMs
    
    fun isActiveAt(timeMs: Long): Boolean {
        return timeMs in startTimeMs..endTimeMs
    }
    
}
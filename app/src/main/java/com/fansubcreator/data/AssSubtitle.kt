package com.fansubcreator.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class AssStyle(
    val name: String = "Default",
    val fontName: String = "Arial",
    val fontSize: Int = 18,
    val primaryColor: Color = Color.White,
    val secondaryColor: Color = Color.Red,
    val outlineColor: Color = Color.Black,
    val shadowColor: Color = Color.Black,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikeout: Boolean = false,
    val scaleX: Float = 100f,
    val scaleY: Float = 100f,
    val spacing: Float = 0f,
    val angle: Float = 0f,
    val borderStyle: Int = 1,
    val outline: Float = 2f,
    val shadow: Float = 0f,
    val alignment: Int = 2,
    val marginL: Int = 10,
    val marginR: Int = 10,
    val marginV: Int = 10
)

data class AssEvent(
    val id: String,
    val start: Long, // milliseconds
    val end: Long,   // milliseconds
    val style: String = "Default",
    val name: String = "",
    val marginL: Int = 0,
    val marginR: Int = 0,
    val marginV: Int = 0,
    val effect: String = "",
    val text: String,
    val layer: Int = 0
) {
    fun getDurationMs(): Long = end - start
    
    fun isActiveAt(timeMs: Long): Boolean = timeMs in start..end
    
    fun toAssFormat(): String {
        return "Dialogue: $layer,${formatTime(start)},${formatTime(end)},$style,$name,$marginL,$marginR,$marginV,$effect,$text"
    }
    
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val centiseconds = (ms % 1000) / 10
        return "%d:%02d:%02d.%02d".format(hours, minutes, seconds, centiseconds)
    }
}

data class AssFile(
    val title: String = "Untitled",
    val originalScript: String = "Unknown",
    val translator: String = "",
    val editor: String = "",
    val timer: String = "",
    val synchPoint: String = "",
    val scriptType: String = "v4.00+",
    val collisions: String = "Normal",
    val playResX: Int = 1920,
    val playResY: Int = 1080,
    val timerValue: Float = 100.0f,
    val wrapStyle: Int = 0,
    val scaledBorderAndShadow: String = "no",
    val styles: List<AssStyle> = listOf(AssStyle()),
    val events: List<AssEvent> = emptyList()
) {
    fun toAssString(): String {
        val sb = StringBuilder()
        
        // Script Info
        sb.appendLine("[Script Info]")
        sb.appendLine("Title: $title")
        sb.appendLine("Original Script: $originalScript")
        sb.appendLine("Translator: $translator")
        sb.appendLine("Editor: $editor")
        sb.appendLine("Timer: $timer")
        sb.appendLine("Synch Point: $synchPoint")
        sb.appendLine("Script Type: $scriptType")
        sb.appendLine("Collisions: $collisions")
        sb.appendLine("PlayResX: $playResX")
        sb.appendLine("PlayResY: $playResY")
        sb.appendLine("Timer: $timerValue")
        sb.appendLine("WrapStyle: $wrapStyle")
        sb.appendLine("ScaledBorderAndShadow: $scaledBorderAndShadow")
        sb.appendLine()
        
        // Styles
        sb.appendLine("[V4+ Styles]")
        sb.appendLine("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding")
        
        styles.forEach { style ->
            sb.appendLine("Style: ${style.name},${style.fontName},${style.fontSize},${colorToAss(style.primaryColor)},${colorToAss(style.secondaryColor)},${colorToAss(style.outlineColor)},${colorToAss(style.shadowColor)},${if (style.bold) -1 else 0},${if (style.italic) -1 else 0},${if (style.underline) -1 else 0},${if (style.strikeout) -1 else 0},${style.scaleX},${style.scaleY},${style.spacing},${style.angle},${style.borderStyle},${style.outline},${style.shadow},${style.alignment},${style.marginL},${style.marginR},${style.marginV},1")
        }
        sb.appendLine()
        
        // Events
        sb.appendLine("[Events]")
        sb.appendLine("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text")
        
        events.forEach { event ->
            sb.appendLine(event.toAssFormat())
        }
        
        return sb.toString()
    }
    
    private fun colorToAss(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return "&H00${String.format("%02X%02X%02X", b, g, r)}"
    }
    
    fun addEvent(event: AssEvent): AssFile {
        return copy(events = events + event)
    }
    
    fun updateEvent(eventId: String, updatedEvent: AssEvent): AssFile {
        return copy(events = events.map { if (it.id == eventId) updatedEvent else it })
    }
    
    fun deleteEvent(eventId: String): AssFile {
        return copy(events = events.filter { it.id != eventId })
    }
    
    fun shiftTiming(offsetMs: Long): AssFile {
        return copy(events = events.map { 
            it.copy(
                start = (it.start + offsetMs).coerceAtLeast(0),
                end = (it.end + offsetMs).coerceAtLeast(0)
            )
        })
    }
}
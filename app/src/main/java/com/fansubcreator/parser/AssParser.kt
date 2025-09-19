package com.fansubcreator.parser

import androidx.compose.ui.graphics.Color
import com.fansubcreator.data.AssEvent
import com.fansubcreator.data.AssFile
import com.fansubcreator.data.AssStyle
import java.util.*

object AssParser {
    
    fun parseAssFile(content: String): AssFile {
        val lines = content.lines()
        var currentSection = ""
        val styles = mutableListOf<AssStyle>()
        val events = mutableListOf<AssEvent>()
        var scriptInfo = mutableMapOf<String, String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.startsWith("[") && trimmedLine.endsWith("]") -> {
                    currentSection = trimmedLine
                }
                trimmedLine.startsWith("Title:") && currentSection == "[Script Info]" -> {
                    scriptInfo["title"] = trimmedLine.substringAfter(":").trim()
                }
                trimmedLine.startsWith("Style:") && currentSection == "[V4+ Styles]" -> {
                    parseStyle(trimmedLine)?.let { styles.add(it) }
                }
                trimmedLine.startsWith("Dialogue:") && currentSection == "[Events]" -> {
                    parseEvent(trimmedLine)?.let { events.add(it) }
                }
            }
        }
        
        return AssFile(
            title = scriptInfo["title"] ?: "Untitled",
            styles = styles.ifEmpty { listOf(AssStyle()) },
            events = events
        )
    }
    
    private fun parseStyle(line: String): AssStyle? {
        return try {
            val parts = line.substringAfter("Style:").split(",").map { it.trim() }
            if (parts.size >= 23) {
                AssStyle(
                    name = parts[0],
                    fontName = parts[1],
                    fontSize = parts[2].toIntOrNull() ?: 18,
                    primaryColor = parseAssColor(parts[3]),
                    secondaryColor = parseAssColor(parts[4]),
                    outlineColor = parseAssColor(parts[5]),
                    shadowColor = parseAssColor(parts[6]),
                    bold = parts[7] == "-1",
                    italic = parts[8] == "-1",
                    underline = parts[9] == "-1",
                    strikeout = parts[10] == "-1",
                    scaleX = parts[11].toFloatOrNull() ?: 100f,
                    scaleY = parts[12].toFloatOrNull() ?: 100f,
                    spacing = parts[13].toFloatOrNull() ?: 0f,
                    angle = parts[14].toFloatOrNull() ?: 0f,
                    borderStyle = parts[15].toIntOrNull() ?: 1,
                    outline = parts[16].toFloatOrNull() ?: 2f,
                    shadow = parts[17].toFloatOrNull() ?: 0f,
                    alignment = parts[18].toIntOrNull() ?: 2,
                    marginL = parts[19].toIntOrNull() ?: 10,
                    marginR = parts[20].toIntOrNull() ?: 10,
                    marginV = parts[21].toIntOrNull() ?: 10
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseEvent(line: String): AssEvent? {
        return try {
            val parts = line.substringAfter("Dialogue:").split(",", limit = 10)
            if (parts.size >= 10) {
                AssEvent(
                    id = UUID.randomUUID().toString(),
                    layer = parts[0].trim().toIntOrNull() ?: 0,
                    start = parseTime(parts[1].trim()),
                    end = parseTime(parts[2].trim()),
                    style = parts[3].trim(),
                    name = parts[4].trim(),
                    marginL = parts[5].trim().toIntOrNull() ?: 0,
                    marginR = parts[6].trim().toIntOrNull() ?: 0,
                    marginV = parts[7].trim().toIntOrNull() ?: 0,
                    effect = parts[8].trim(),
                    text = parts[9]
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseTime(timeStr: String): Long {
        return try {
            val parts = timeStr.split(":")
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            val secParts = parts[2].split(".")
            val seconds = secParts[0].toLong()
            val centiseconds = secParts[1].toLong()
            
            (hours * 3600 + minutes * 60 + seconds) * 1000 + centiseconds * 10
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun parseAssColor(colorStr: String): Color {
        return try {
            if (colorStr.startsWith("&H")) {
                val hex = colorStr.substring(2).padStart(8, '0')
                val b = Integer.parseInt(hex.substring(2, 4), 16)
                val g = Integer.parseInt(hex.substring(4, 6), 16)
                val r = Integer.parseInt(hex.substring(6, 8), 16)
                Color(r / 255f, g / 255f, b / 255f)
            } else {
                Color.White
            }
        } catch (e: Exception) {
            Color.White
        }
    }
    
    fun generateSrtFromAss(assFile: AssFile): String {
        val sb = StringBuilder()
        assFile.events.forEachIndexed { index, event ->
            sb.appendLine("${index + 1}")
            sb.appendLine("${formatSrtTime(event.start)} --> ${formatSrtTime(event.end)}")
            sb.appendLine(cleanAssText(event.text))
            sb.appendLine()
        }
        return sb.toString()
    }
    
    private fun formatSrtTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val milliseconds = ms % 1000
        return "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, milliseconds)
    }
    
    private fun cleanAssText(text: String): String {
        // Remove ASS formatting tags for SRT export
        return text.replace(Regex("\\{[^}]*\\}"), "")
            .replace("\\N", "\n")
            .replace("\\n", "\n")
    }
}
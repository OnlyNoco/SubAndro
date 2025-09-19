package com.fansubcreator.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.fansubcreator.data.AssFile
import com.fansubcreator.parser.AssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class FileManager(private val context: Context) {
    
    suspend fun readTextFile(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            reader.close()
            inputStream?.close()
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun writeTextFile(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            val writer = OutputStreamWriter(outputStream)
            writer.write(content)
            writer.close()
            outputStream?.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadAssFile(uri: Uri): Result<AssFile> = withContext(Dispatchers.IO) {
        try {
            val content = readTextFile(uri).getOrThrow()
            val assFile = AssParser.parseAssFile(content)
            Result.success(assFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveAssFile(uri: Uri, assFile: AssFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = assFile.toAssString()
            writeTextFile(uri, content).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportToSrt(uri: Uri, assFile: AssFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val srtContent = AssParser.generateSrtFromAss(assFile)
            writeTextFile(uri, srtContent).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadTextClipboard(uri: Uri): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val content = readTextFile(uri).getOrThrow()
            val lines = content.lines().filter { it.isNotBlank() }
            Result.success(lines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveTextClipboard(uri: Uri, lines: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = lines.joinToString("\n")
            writeTextFile(uri, content).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun createDocumentIntent(mimeType: String, filename: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, filename)
        }
    }
    
    fun openDocumentIntent(mimeType: String): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }
    }
    
    companion object {
        const val ASS_MIME_TYPE = "text/plain"
        const val SRT_MIME_TYPE = "text/plain"
        const val TXT_MIME_TYPE = "text/plain"
        const val VIDEO_MIME_TYPE = "video/*"
        
        fun getFileExtension(uri: Uri): String {
            return uri.toString().substringAfterLast('.', "")
        }
        
        fun isVideoFile(uri: Uri): Boolean {
            val extension = getFileExtension(uri).lowercase()
            return extension in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v")
        }
        
        fun isSubtitleFile(uri: Uri): Boolean {
            val extension = getFileExtension(uri).lowercase()
            return extension in listOf("ass", "ssa", "srt", "vtt", "sub")
        }
        
        fun isTextFile(uri: Uri): Boolean {
            val extension = getFileExtension(uri).lowercase()
            return extension in listOf("txt", "text")
        }
    }
}

// Font management for ASS subtitles
class FontManager(private val context: Context) {
    
    data class FontInfo(
        val name: String,
        val path: String,
        val isSystemFont: Boolean = false
    )
    
    fun getSystemFonts(): List<FontInfo> {
        return listOf(
            FontInfo("Arial", "Arial", true),
            FontInfo("Times New Roman", "Times New Roman", true),
            FontInfo("Helvetica", "Helvetica", true),
            FontInfo("Georgia", "Georgia", true),
            FontInfo("Verdana", "Verdana", true),
            FontInfo("Trebuchet MS", "Trebuchet MS", true),
            FontInfo("Comic Sans MS", "Comic Sans MS", true),
            FontInfo("Impact", "Impact", true),
            FontInfo("Lucida Console", "Lucida Console", true),
            FontInfo("Tahoma", "Tahoma", true),
            FontInfo("Courier New", "Courier New", true)
        )
    }
    
    fun getCustomFonts(): List<FontInfo> {
        val customFonts = mutableListOf<FontInfo>()
        
        try {
            val fontsDir = DocumentFile.fromTreeUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AFonts"))
            fontsDir?.listFiles()?.forEach { file ->
                if (file.name?.endsWith(".ttf", ignoreCase = true) == true || 
                    file.name?.endsWith(".otf", ignoreCase = true) == true) {
                    customFonts.add(
                        FontInfo(
                            name = file.name!!.substringBeforeLast('.'),
                            path = file.uri.toString(),
                            isSystemFont = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle font directory access error
        }
        
        return customFonts
    }
    
    fun getAllFonts(): List<FontInfo> {
        return getSystemFonts() + getCustomFonts()
    }
    
    suspend fun installFont(uri: Uri): Result<FontInfo> = withContext(Dispatchers.IO) {
        try {
            val fileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "unknown.ttf"
            val fontName = fileName.substringBeforeLast('.')
            
            // Copy font to app's private directory for use
            val internalFontFile = java.io.File(context.filesDir, "fonts/$fileName")
            internalFontFile.parentFile?.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                internalFontFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            val fontInfo = FontInfo(
                name = fontName,
                path = internalFontFile.absolutePath,
                isSystemFont = false
            )
            
            Result.success(fontInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
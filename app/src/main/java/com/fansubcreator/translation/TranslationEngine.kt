package com.fansubcreator.translation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

interface TranslationEngine {
    suspend fun translate(text: String, fromLang: String, toLang: String): Result<String>
    fun getSupportedLanguages(): List<Language>
}

data class Language(
    val code: String,
    val name: String,
    val flag: String = ""
)

class GoogleTranslateEngine : TranslationEngine {
    private val _isOnline = mutableStateOf(false)
    val isOnline: State<Boolean> = _isOnline
    
    override suspend fun translate(text: String, fromLang: String, toLang: String): Result<String> {
        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$fromLang&tl=$toLang&dt=t&q=$encodedText"
            
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                
                // Parse the response (simplified JSON parsing)
                val translatedText = parseGoogleTranslateResponse(response)
                _isOnline.value = true
                Result.success(translatedText)
            } else {
                _isOnline.value = false
                Result.failure(Exception("Translation failed: HTTP $responseCode"))
            }
        } catch (e: Exception) {
            _isOnline.value = false
            Result.failure(e)
        }
    }
    
    private fun parseGoogleTranslateResponse(response: String): String {
        return try {
            // Simple parsing for Google Translate response format
            val startIndex = response.indexOf("\"") + 1
            val endIndex = response.indexOf("\"", startIndex)
            response.substring(startIndex, endIndex)
        } catch (e: Exception) {
            "Translation failed"
        }
    }
    
    override fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language("en", "English", "🇺🇸"),
            Language("ja", "Japanese", "🇯🇵"),
            Language("ko", "Korean", "🇰🇷"),
            Language("zh", "Chinese", "🇨🇳"),
            Language("es", "Spanish", "🇪🇸"),
            Language("fr", "French", "🇫🇷"),
            Language("de", "German", "🇩🇪"),
            Language("it", "Italian", "🇮🇹"),
            Language("pt", "Portuguese", "🇵🇹"),
            Language("ru", "Russian", "🇷🇺"),
            Language("ar", "Arabic", "🇸🇦"),
            Language("hi", "Hindi", "🇮🇳"),
            Language("th", "Thai", "🇹🇭"),
            Language("vi", "Vietnamese", "🇻🇳"),
            Language("tr", "Turkish", "🇹🇷"),
            Language("pl", "Polish", "🇵🇱"),
            Language("nl", "Dutch", "🇳🇱"),
            Language("sv", "Swedish", "🇸🇪"),
            Language("da", "Danish", "🇩🇰"),
            Language("no", "Norwegian", "🇳🇴")
        )
    }
}

class OfflineTranslationEngine : TranslationEngine {
    // Simple dictionary-based translation for offline use
    private val commonPhrases = mapOf(
        // Japanese to English
        "こんにちは" to "Hello",
        "ありがとう" to "Thank you",
        "さようなら" to "Goodbye",
        "はい" to "Yes",
        "いいえ" to "No",
        "すみません" to "Excuse me",
        "わかりません" to "I don't understand",
        
        // Korean to English
        "안녕하세요" to "Hello",
        "감사합니다" to "Thank you",
        "안녕히 가세요" to "Goodbye",
        "네" to "Yes",
        "아니요" to "No",
        
        // Common anime/drama terms
        "先輩" to "Senpai",
        "後輩" to "Kouhai",
        "先生" to "Sensei",
        "お疲れ様" to "Good work",
        "頑張って" to "Do your best",
        "愛している" to "I love you",
        "大好き" to "I really like you"
    )
    
    override suspend fun translate(text: String, fromLang: String, toLang: String): Result<String> {
        return try {
            delay(200) // Simulate processing time
            
            // Try exact match first
            val exactMatch = commonPhrases[text]
            if (exactMatch != null) {
                return Result.success(exactMatch)
            }
            
            // Try partial matches
            val partialMatches = commonPhrases.keys.filter { key ->
                text.contains(key) || key.contains(text)
            }
            
            if (partialMatches.isNotEmpty()) {
                val bestMatch = partialMatches.first()
                val translation = commonPhrases[bestMatch] ?: text
                Result.success("$translation (partial match)")
            } else {
                Result.success("$text (no translation found)")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language("ja", "Japanese", "🇯🇵"),
            Language("ko", "Korean", "🇰🇷"),
            Language("en", "English", "🇺🇸")
        )
    }
}

class TranslationManager {
    private val googleEngine = GoogleTranslateEngine()
    private val offlineEngine = OfflineTranslationEngine()
    
    suspend fun translate(
        text: String,
        fromLang: String,
        toLang: String,
        useOnline: Boolean = true
    ): Result<String> {
        return try {
            if (useOnline) {
                val result = googleEngine.translate(text, fromLang, toLang)
                if (result.isSuccess) {
                    result
                } else {
                    // Fallback to offline
                    offlineEngine.translate(text, fromLang, toLang)
                }
            } else {
                offlineEngine.translate(text, fromLang, toLang)
            }
        } catch (e: Exception) {
            // Always fallback to offline on any error
            offlineEngine.translate(text, fromLang, toLang)
        }
    }
    
    fun getSupportedLanguages(includeOnline: Boolean = true): List<Language> {
        return if (includeOnline) {
            googleEngine.getSupportedLanguages()
        } else {
            offlineEngine.getSupportedLanguages()
        }
    }
    
    suspend fun batchTranslate(
        texts: List<String>,
        fromLang: String,
        toLang: String,
        useOnline: Boolean = true,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Result<String>> {
        val results = mutableListOf<Result<String>>()
        
        texts.forEachIndexed { index, text ->
            val result = translate(text, fromLang, toLang, useOnline)
            results.add(result)
            onProgress(index + 1, texts.size)
            
            // Add delay between requests to avoid rate limiting
            if (useOnline) {
                delay(500)
            }
        }
        
        return results
    }
}
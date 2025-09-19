package com.fansubcreator.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fansubcreator.data.AssEvent
import com.fansubcreator.data.AssFile
import com.fansubcreator.data.AssStyle
import com.fansubcreator.translation.TranslationManager
import com.fansubcreator.ui.components.VoiceSegment
import com.fansubcreator.utils.FileManager
import kotlinx.coroutines.launch
import java.util.*

class AdvancedSubtitleViewModel : ViewModel() {
    
    // Main subtitle project
    var assFile by mutableStateOf<AssFile?>(null)
        private set
    
    // Video and playback state
    var videoUri by mutableStateOf<Uri?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var currentTimeMs by mutableStateOf(0L)
        private set
    var videoDurationMs by mutableStateOf(0L)
        private set
    
    // Selection and editing
    var selectedEventId by mutableStateOf<String?>(null)
        private set
    var selectedStyleName by mutableStateOf<String?>(null)
        private set
    
    // Voice recognition
    var isRecording by mutableStateOf(false)
        private set
    var voiceSegments by mutableStateOf<List<VoiceSegment>>(emptyList())
        private set
    
    // Translation
    var originalTexts by mutableStateOf<List<String>>(emptyList())
        private set
    var translatedTexts by mutableStateOf<List<String>>(emptyList())
        private set
    
    // Text clipboard
    var textClipboard by mutableStateOf("")
        private set
    var clipboardLines by mutableStateOf<List<String>>(emptyList())
        private set
    
    // UI state
    var showExportDialog by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    
    private val translationManager = TranslationManager()
    
    // Initialize new project
    fun createProject(videoUri: Uri, projectName: String = "New Project") {
        this.videoUri = videoUri
        this.assFile = AssFile(
            title = projectName,
            styles = listOf(AssStyle()),
            events = emptyList()
        )
        clearError()
    }
    
    // Load existing ASS project
    fun loadProject(fileManager: FileManager, assUri: Uri) {
        viewModelScope.launch {
            isLoading = true
            fileManager.loadAssFile(assUri)
                .onSuccess { loadedAssFile ->
                    assFile = loadedAssFile
                    updateOriginalTexts()
                    clearError()
                }
                .onFailure { error ->
                    errorMessage = "Failed to load project: ${error.message}"
                }
            isLoading = false
        }
    }
    
    // Save project
    fun saveProject(fileManager: FileManager, uri: Uri) {
        viewModelScope.launch {
            assFile?.let { file ->
                isLoading = true
                fileManager.saveAssFile(uri, file)
                    .onSuccess { clearError() }
                    .onFailure { error ->
                        errorMessage = "Failed to save project: ${error.message}"
                    }
                isLoading = false
            }
        }
    }
    
    // Export to SRT
    fun exportToSrt(fileManager: FileManager, uri: Uri) {
        viewModelScope.launch {
            assFile?.let { file ->
                isLoading = true
                fileManager.exportToSrt(uri, file)
                    .onSuccess { 
                        clearError()
                        showExportDialog = false
                    }
                    .onFailure { error ->
                        errorMessage = "Failed to export: ${error.message}"
                    }
                isLoading = false
            }
        }
    }
    
    // Playback control
    fun updatePlaybackState(isPlaying: Boolean, currentTimeMs: Long, durationMs: Long = videoDurationMs) {
        this.isPlaying = isPlaying
        this.currentTimeMs = currentTimeMs
        if (durationMs > 0) {
            this.videoDurationMs = durationMs
        }
    }
    
    // Event management
    fun addEventAtCurrentTime(text: String = "New subtitle", style: String = "Default") {
        assFile?.let { file ->
            val newEvent = AssEvent(
                id = UUID.randomUUID().toString(),
                start = currentTimeMs,
                end = currentTimeMs + 3000, // 3 seconds duration
                text = text,
                style = style
            )
            assFile = file.addEvent(newEvent)
            selectedEventId = newEvent.id
            updateOriginalTexts()
        }
    }
    
    fun updateEvent(event: AssEvent) {
        assFile?.let { file ->
            assFile = file.updateEvent(event.id, event)
            updateOriginalTexts()
        }
    }
    
    fun deleteEvent(eventId: String) {
        assFile?.let { file ->
            assFile = file.deleteEvent(eventId)
            if (selectedEventId == eventId) {
                selectedEventId = null
            }
            updateOriginalTexts()
        }
    }
    
    fun selectEvent(eventId: String?) {
        selectedEventId = eventId
    }
    
    // Style management
    fun updateStyle(style: AssStyle) {
        assFile?.let { file ->
            val updatedStyles = file.styles.map { 
                if (it.name == style.name) style else it 
            }
            assFile = file.copy(styles = updatedStyles)
        }
    }
    
    fun selectStyle(styleName: String?) {
        selectedStyleName = styleName
    }
    
    // Timing operations
    fun shiftAllTiming(offsetMs: Long) {
        assFile?.let { file ->
            assFile = file.shiftTiming(offsetMs)
        }
    }
    
    fun shiftSelectedTiming(offsetMs: Long) {
        selectedEventId?.let { eventId ->
            assFile?.events?.find { it.id == eventId }?.let { event ->
                val updatedEvent = event.copy(
                    start = (event.start + offsetMs).coerceAtLeast(0),
                    end = (event.end + offsetMs).coerceAtLeast(0)
                )
                updateEvent(updatedEvent)
            }
        }
    }
    
    // Voice recognition
    fun startRecording() {
        isRecording = true
        // TODO: Implement actual audio recording and VAD
    }
    
    fun stopRecording() {
        isRecording = false
        // TODO: Process recorded audio and generate voice segments
    }
    
    fun addVoiceSegment(segment: VoiceSegment) {
        voiceSegments = voiceSegments + segment
    }
    
    fun clearVoiceSegments() {
        voiceSegments = emptyList()
    }
    
    fun selectVoiceSegment(segment: VoiceSegment) {
        // Create subtitle from voice segment
        val newEvent = AssEvent(
            id = UUID.randomUUID().toString(),
            start = segment.startTimeMs,
            end = segment.endTimeMs,
            text = segment.text.ifEmpty { "New subtitle" },
            style = "Default"
        )
        
        assFile?.let { file ->
            assFile = file.addEvent(newEvent)
            selectedEventId = newEvent.id
            updateOriginalTexts()
        }
    }
    
    // Translation
    private fun updateOriginalTexts() {
        assFile?.let { file ->
            originalTexts = file.events.map { it.text }
        }
    }
    
    fun updateTranslatedTexts(translations: List<String>) {
        translatedTexts = translations
    }
    
    fun applyTranslationsToSubtitles() {
        assFile?.let { file ->
            val updatedEvents = file.events.mapIndexed { index, event ->
                if (index < translatedTexts.size && translatedTexts[index].isNotEmpty()) {
                    event.copy(text = translatedTexts[index])
                } else event
            }
            assFile = file.copy(events = updatedEvents)
            updateOriginalTexts()
        }
    }
    
    // Text clipboard
    fun updateTextClipboard(text: String) {
        textClipboard = text
        clipboardLines = text.lines().filter { it.isNotBlank() }
    }
    
    fun loadTextClipboard(fileManager: FileManager, uri: Uri) {
        viewModelScope.launch {
            fileManager.loadTextClipboard(uri)
                .onSuccess { lines ->
                    clipboardLines = lines
                    textClipboard = lines.joinToString("\n")
                    clearError()
                }
                .onFailure { error ->
                    errorMessage = "Failed to load text file: ${error.message}"
                }
        }
    }
    
    fun saveTextClipboard(fileManager: FileManager, uri: Uri) {
        viewModelScope.launch {
            fileManager.saveTextClipboard(uri, clipboardLines)
                .onSuccess { clearError() }
                .onFailure { error ->
                    errorMessage = "Failed to save text file: ${error.message}"
                }
        }
    }
    
    // Utility
    fun getActiveEvents(): List<AssEvent> {
        return assFile?.events?.filter { it.isActiveAt(currentTimeMs) } ?: emptyList()
    }
    
    fun getSelectedEvent(): AssEvent? {
        return selectedEventId?.let { id ->
            assFile?.events?.find { it.id == id }
        }
    }
    
    fun getSelectedStyle(): AssStyle? {
        return selectedStyleName?.let { name ->
            assFile?.styles?.find { it.name == name }
        }
    }
    
    // UI state management
    fun showExportDialog() {
        showExportDialog = true
    }
    
    fun hideExportDialog() {
        showExportDialog = false
    }
    
    fun clearError() {
        errorMessage = null
    }
}
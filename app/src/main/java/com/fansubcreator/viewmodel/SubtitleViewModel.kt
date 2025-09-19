package com.fansubcreator.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.fansubcreator.data.SubtitleCue
import com.fansubcreator.data.SubtitleProject
import java.util.*

class SubtitleViewModel : ViewModel() {
    var project by mutableStateOf<SubtitleProject?>(null)
        private set
    
    var isPlaying by mutableStateOf(false)
        private set
    
    var currentTimeMs by mutableStateOf(0L)
        private set
    
    var selectedSubtitleId by mutableStateOf<String?>(null)
        private set
    
    var showSubtitleEditor by mutableStateOf(false)
        private set
    
    fun createProject(videoUri: Uri, projectName: String) {
        project = SubtitleProject(
            id = UUID.randomUUID().toString(),
            name = projectName,
            videoUri = videoUri
        )
    }
    
    fun updatePlaybackState(isPlaying: Boolean, currentTimeMs: Long) {
        this.isPlaying = isPlaying
        this.currentTimeMs = currentTimeMs
        project?.let {
            project = it.copy(currentTimeMs = currentTimeMs)
        }
    }
    
    fun addSubtitleAtCurrentTime(text: String = "New subtitle") {
        project?.let { currentProject ->
            val newSubtitle = SubtitleCue(
                id = UUID.randomUUID().toString(),
                text = text,
                startTimeMs = currentTimeMs,
                endTimeMs = currentTimeMs + 3000, // 3 seconds duration
                position = Offset(0.5f, 0.8f), // Default position (center bottom)
                fontSize = 18.sp,
                fontColor = Color.White,
                backgroundColor = Color.Black.copy(alpha = 0.7f)
            )
            project = currentProject.addSubtitle(newSubtitle)
            selectedSubtitleId = newSubtitle.id
            showSubtitleEditor = true
        }
    }
    
    fun updateSubtitleText(subtitleId: String, newText: String) {
        project?.let { currentProject ->
            val existingSubtitle = currentProject.subtitles.find { it.id == subtitleId }
            existingSubtitle?.let { subtitle ->
                val updatedSubtitle = subtitle.copy(text = newText)
                project = currentProject.updateSubtitle(subtitleId, updatedSubtitle)
            }
        }
    }
    
    fun updateSubtitlePosition(subtitleId: String, newPosition: Offset) {
        project?.let { currentProject ->
            val existingSubtitle = currentProject.subtitles.find { it.id == subtitleId }
            existingSubtitle?.let { subtitle ->
                val updatedSubtitle = subtitle.copy(position = newPosition)
                project = currentProject.updateSubtitle(subtitleId, updatedSubtitle)
            }
        }
    }
    
    fun updateSubtitleTiming(subtitleId: String, startMs: Long, endMs: Long) {
        project?.let { currentProject ->
            val existingSubtitle = currentProject.subtitles.find { it.id == subtitleId }
            existingSubtitle?.let { subtitle ->
                val updatedSubtitle = subtitle.copy(startTimeMs = startMs, endTimeMs = endMs)
                project = currentProject.updateSubtitle(subtitleId, updatedSubtitle)
            }
        }
    }
    
    fun updateSubtitleStyle(
        subtitleId: String,
        fontSize: Float? = null,
        fontColor: Color? = null,
        backgroundColor: Color? = null
    ) {
        project?.let { currentProject ->
            val existingSubtitle = currentProject.subtitles.find { it.id == subtitleId }
            existingSubtitle?.let { subtitle ->
                val updatedSubtitle = subtitle.copy(
                    fontSize = fontSize?.sp ?: subtitle.fontSize,
                    fontColor = fontColor ?: subtitle.fontColor,
                    backgroundColor = backgroundColor ?: subtitle.backgroundColor
                )
                project = currentProject.updateSubtitle(subtitleId, updatedSubtitle)
            }
        }
    }
    
    fun selectSubtitle(subtitleId: String?) {
        selectedSubtitleId = subtitleId
        showSubtitleEditor = subtitleId != null
    }
    
    fun closeSubtitleEditor() {
        showSubtitleEditor = false
        selectedSubtitleId = null
    }
    
    fun deleteSubtitle(subtitleId: String) {
        project?.let { currentProject ->
            project = currentProject.removeSubtitle(subtitleId)
            if (selectedSubtitleId == subtitleId) {
                closeSubtitleEditor()
            }
        }
    }
    
    fun getActiveSubtitles(): List<SubtitleCue> {
        return project?.getActiveSubtitlesAt(currentTimeMs) ?: emptyList()
    }
}
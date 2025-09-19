package com.fansubcreator.data

import android.net.Uri

data class SubtitleProject(
    val id: String,
    val name: String,
    val videoUri: Uri,
    val subtitles: List<SubtitleCue> = emptyList(),
    val currentTimeMs: Long = 0L,
    val videoDurationMs: Long = 0L
) {
    fun addSubtitle(subtitle: SubtitleCue): SubtitleProject {
        return copy(subtitles = subtitles + subtitle)
    }
    
    fun updateSubtitle(subtitleId: String, updatedSubtitle: SubtitleCue): SubtitleProject {
        return copy(
            subtitles = subtitles.map { 
                if (it.id == subtitleId) updatedSubtitle else it 
            }
        )
    }
    
    fun removeSubtitle(subtitleId: String): SubtitleProject {
        return copy(subtitles = subtitles.filter { it.id != subtitleId })
    }
    
    fun getActiveSubtitlesAt(timeMs: Long): List<SubtitleCue> {
        return subtitles.filter { it.isActiveAt(timeMs) }
    }
}
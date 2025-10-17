package com.movie.securevideoapp.models

import java.util.UUID

data class Episode(
    val id: String = UUID.randomUUID().toString(),
    val seasonId: String,
    val number: Int,
    val title: String,
    val videoUri: String,
    val duration: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,
    val watched: Boolean = false,
    val lastPosition: Long = 0
) {
    fun getDisplayTitle(): String {
        return "Episódio $number${if (title.isNotBlank()) " - $title" else ""}"
    }
    
    fun getProgressPercentage(): Int {
        return if (duration > 0) {
            ((lastPosition.toFloat() / duration.toFloat()) * 100).toInt()
        } else 0
    }
    
    fun hasProgress(): Boolean = lastPosition > 0 && lastPosition < duration
}

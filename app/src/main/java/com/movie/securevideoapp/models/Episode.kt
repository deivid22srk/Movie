package com.movie.securevideoapp.models

import java.util.UUID

data class Episode(
    val id: String = UUID.randomUUID().toString(),
    val seasonId: String,
    val number: Int,
    val title: String,
    val encryptedVideoPath: String,
    val duration: Long = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,
    val watched: Boolean = false
) {
    fun getDisplayTitle(): String {
        return "Episódio $number${if (title.isNotBlank()) " - $title" else ""}"
    }
}

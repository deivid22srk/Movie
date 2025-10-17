package com.movie.securevideoapp.models

import java.util.UUID

data class Season(
    val id: String = UUID.randomUUID().toString(),
    val seriesId: String,
    val number: Int,
    val title: String = "",
    val dateCreated: Long = System.currentTimeMillis()
) {
    fun getDisplayTitle(): String {
        return "Temporada $number${if (title.isNotBlank()) " - $title" else ""}"
    }
}

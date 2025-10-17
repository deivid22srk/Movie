package com.movie.securevideoapp.models

import java.util.UUID

data class Series(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val posterPath: String? = null,
    val dateCreated: Long = System.currentTimeMillis()
)

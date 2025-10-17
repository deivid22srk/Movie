package com.movie.securevideoapp.models

data class WatchProgress(
    val episodeId: String,
    val position: Long = 0,
    val lastWatched: Long = System.currentTimeMillis()
)

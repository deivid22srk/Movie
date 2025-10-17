package com.movie.securevideoapp.utils

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ContentDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.upstream.DefaultDataSource

@UnstableApi
class ContentUriDataSourceFactory(private val context: Context) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return DefaultDataSource.Factory(context)
            .createDataSource()
    }
}

@UnstableApi
fun createContentDataSourceFactory(context: Context): DataSource.Factory {
    return DefaultDataSource.Factory(context)
}

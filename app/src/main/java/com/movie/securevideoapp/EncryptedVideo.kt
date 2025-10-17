package com.movie.securevideoapp

import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Video {
    abstract val id: String
    abstract val name: String
    abstract val size: Long
    abstract val dateAdded: Long
    abstract val isEncrypted: Boolean
    
    val formattedSize: String
        get() = formatFileSize(size)
    
    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(dateAdded))
    
    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "Desconhecido"
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }
    
    data class LocalEncrypted(
        val file: File,
        override val name: String = file.nameWithoutExtension,
        override val size: Long = file.length(),
        override val dateAdded: Long = file.lastModified()
    ) : Video() {
        override val id: String = file.absolutePath
        override val isEncrypted: Boolean = true
    }
    
    data class RemoteStream(
        val uri: Uri,
        override val name: String,
        override val size: Long = 0,
        override val dateAdded: Long = System.currentTimeMillis()
    ) : Video() {
        override val id: String = uri.toString()
        override val isEncrypted: Boolean = false
    }
}

typealias EncryptedVideo = Video.LocalEncrypted

package com.movie.securevideoapp

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EncryptedVideo(
    val file: File,
    val name: String = file.nameWithoutExtension,
    val size: Long = file.length(),
    val dateAdded: Long = file.lastModified()
) {
    val formattedSize: String
        get() = formatFileSize(size)
    
    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(dateAdded))
    
    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }
}

package com.movie.securevideoapp.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {
    
    suspend fun createBackup(outputUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    val filesToBackup = listOf(
                        "series.json",
                        "seasons.json",
                        "episodes.json",
                        "progress.json"
                    )
                    
                    filesToBackup.forEach { fileName ->
                        val file = File(context.filesDir, fileName)
                        if (file.exists()) {
                            FileInputStream(file).use { fis ->
                                val zipEntry = ZipEntry(fileName)
                                zipOut.putNextEntry(zipEntry)
                                fis.copyTo(zipOut)
                                zipOut.closeEntry()
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun restoreBackup(inputUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val file = File(context.filesDir, entry.name)
                        FileOutputStream(file).use { fos ->
                            zipIn.copyTo(fos)
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getBackupInfo(): BackupInfo {
        val seriesFile = File(context.filesDir, "series.json")
        val seasonsFile = File(context.filesDir, "seasons.json")
        val episodesFile = File(context.filesDir, "episodes.json")
        
        val lastModified = listOf(seriesFile, seasonsFile, episodesFile)
            .filter { it.exists() }
            .maxOfOrNull { it.lastModified() } ?: 0L
        
        val totalSize = listOf(seriesFile, seasonsFile, episodesFile)
            .filter { it.exists() }
            .sumOf { it.length() }
        
        return BackupInfo(
            lastModified = lastModified,
            totalSize = totalSize,
            hasData = seriesFile.exists() || seasonsFile.exists() || episodesFile.exists()
        )
    }
}

data class BackupInfo(
    val lastModified: Long,
    val totalSize: Long,
    val hasData: Boolean
)

package com.movie.securevideoapp

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object VideoEncryptor {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY = "SecureVideoApp2024SecureKey!!"
    private const val BUFFER_SIZE = 8192
    
    private val secretKey: SecretKeySpec by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(KEY.toByteArray())
        SecretKeySpec(keyBytes, "AES")
    }
    
    private val iv: IvParameterSpec by lazy {
        val ivBytes = ByteArray(16) { it.toByte() }
        IvParameterSpec(ivBytes)
    }
    
    suspend fun encryptVideo(
        context: Context,
        inputUri: Uri,
        outputFileName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val outputDir = File(context.filesDir, "encrypted_videos")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val outputFile = File(outputDir, "$outputFileName.enc")
            
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val cipher = Cipher.getInstance(ALGORITHM)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
                    
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        val output = cipher.update(buffer, 0, bytesRead)
                        output?.let { outputStream.write(it) }
                    }
                    
                    val finalOutput = cipher.doFinal()
                    finalOutput?.let { outputStream.write(it) }
                }
            }
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getEncryptedVideosDir(context: Context): File {
        val dir = File(context.filesDir, "encrypted_videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    fun getAllEncryptedVideos(context: Context): List<File> {
        val dir = getEncryptedVideosDir(context)
        return dir.listFiles()?.filter { it.extension == "enc" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun deleteEncryptedVideo(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun createDecryptionCipher(): Cipher {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        return cipher
    }
}

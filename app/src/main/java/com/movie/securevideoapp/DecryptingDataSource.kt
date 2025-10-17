package com.movie.securevideoapp

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import java.io.File
import java.io.FileInputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

class DecryptingDataSource(
    private val cipher: Cipher
) : BaseDataSource(true) {
    
    private var inputStream: CipherInputStream? = null
    private var file: File? = null
    private var bytesRemaining: Long = 0
    
    override fun open(dataSpec: DataSpec): Long {
        val uri = dataSpec.uri
        file = File(uri.path ?: throw IllegalArgumentException("Invalid URI"))
        
        if (!file!!.exists()) {
            throw IllegalArgumentException("File does not exist: ${file!!.absolutePath}")
        }
        
        val fileInputStream = FileInputStream(file)
        inputStream = CipherInputStream(fileInputStream, cipher)
        
        bytesRemaining = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            C.LENGTH_UNSET.toLong()
        }
        
        if (dataSpec.position > 0) {
            inputStream?.skip(dataSpec.position)
        }
        
        transferStarted(dataSpec)
        return bytesRemaining
    }
    
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }
        
        val bytesRead = inputStream?.read(buffer, offset, length) ?: C.RESULT_END_OF_INPUT
        
        if (bytesRead == -1) {
            return C.RESULT_END_OF_INPUT
        }
        
        if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
            bytesRemaining -= bytesRead.toLong()
        }
        
        bytesTransferred(bytesRead)
        return bytesRead
    }
    
    override fun getUri(): Uri? {
        return file?.let { Uri.fromFile(it) }
    }
    
    override fun close() {
        try {
            inputStream?.close()
        } finally {
            inputStream = null
            file = null
            bytesRemaining = 0
        }
    }
    
    class Factory : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return DecryptingDataSource(VideoEncryptor.createDecryptionCipher())
        }
    }
}

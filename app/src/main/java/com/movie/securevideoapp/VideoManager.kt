package com.movie.securevideoapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object VideoManager {
    private const val PREFS_NAME = "video_manager_prefs"
    private const val KEY_REMOTE_VIDEOS = "remote_videos"
    
    suspend fun saveRemoteVideoUri(
        context: Context,
        uri: Uri,
        name: String
    ): Video.RemoteStream? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val existingVideos = prefs.getString(KEY_REMOTE_VIDEOS, "[]")
            val jsonArray = JSONArray(existingVideos)
            
            val videoJson = JSONObject().apply {
                put("uri", uri.toString())
                put("name", name)
                put("dateAdded", System.currentTimeMillis())
            }
            
            jsonArray.put(videoJson)
            
            prefs.edit().putString(KEY_REMOTE_VIDEOS, jsonArray.toString()).apply()
            
            Video.RemoteStream(
                uri = uri,
                name = name,
                dateAdded = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getAllRemoteVideos(context: Context): List<Video.RemoteStream> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val videosJson = prefs.getString(KEY_REMOTE_VIDEOS, "[]")
            val jsonArray = JSONArray(videosJson)
            
            val videos = mutableListOf<Video.RemoteStream>()
            for (i in 0 until jsonArray.length()) {
                val videoJson = jsonArray.getJSONObject(i)
                videos.add(
                    Video.RemoteStream(
                        uri = Uri.parse(videoJson.getString("uri")),
                        name = videoJson.getString("name"),
                        dateAdded = videoJson.getLong("dateAdded")
                    )
                )
            }
            videos
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun deleteRemoteVideo(context: Context, video: Video.RemoteStream): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val videosJson = prefs.getString(KEY_REMOTE_VIDEOS, "[]")
            val jsonArray = JSONArray(videosJson)
            
            val newArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val videoJson = jsonArray.getJSONObject(i)
                if (videoJson.getString("uri") != video.uri.toString()) {
                    newArray.put(videoJson)
                }
            }
            
            prefs.edit().putString(KEY_REMOTE_VIDEOS, newArray.toString()).apply()
            
            try {
                context.contentResolver.releasePersistableUriPermission(
                    video.uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getAllVideos(context: Context): List<Video> {
        val localVideos = VideoEncryptor.getAllEncryptedVideos(context)
            .map { Video.LocalEncrypted(it) }
        val remoteVideos = getAllRemoteVideos(context)
        
        return (localVideos + remoteVideos).sortedByDescending { it.dateAdded }
    }
}

package com.movie.securevideoapp.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.movie.securevideoapp.data.SeriesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodePlayerScreen(
    episodeId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val seriesManager = remember { SeriesManager(context) }
    
    var episode by remember { mutableStateOf<com.movie.securevideoapp.models.Episode?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    LaunchedEffect(episodeId) {
        episode = seriesManager.getEpisodeById(episodeId)
        episode?.let { ep ->
            try {
                val uri = Uri.parse(ep.videoUri)
                val mediaItem = MediaItem.fromUri(uri)
                
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                
                if (ep.lastPosition > 0) {
                    exoPlayer.seekTo(ep.lastPosition)
                }
                
                exoPlayer.playWhenReady = true
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            delay(5000)
            val position = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (position > 0 && duration > 0) {
                seriesManager.updateEpisodeProgress(episodeId, position, duration)
            }
        }
    }
    
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    scope.launch {
                        val duration = exoPlayer.duration
                        seriesManager.updateEpisodeProgress(episodeId, duration, duration)
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        
        onDispose {
            scope.launch {
                val position = exoPlayer.currentPosition
                val duration = exoPlayer.duration
                if (position > 0 && duration > 0) {
                    seriesManager.updateEpisodeProgress(episodeId, position, duration)
                }
            }
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    BackHandler {
        onBackClick()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        controllerShowTimeoutMs = 3000
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    videoPath: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse(videoPath)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    BackHandler {
        onBackClick()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 3000
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }
    }
}

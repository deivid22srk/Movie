package com.movie.securevideoapp.ui.screens

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
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
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val seriesManager = remember { SeriesManager(context) }
    
    var episode by remember { mutableStateOf<com.movie.securevideoapp.models.Episode?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isFullscreen by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    LaunchedEffect(episodeId) {
        Log.d("EpisodePlayer", "Loading episode: $episodeId")
        episode = seriesManager.getEpisodeById(episodeId)
        episode?.let { ep ->
            try {
                Log.d("EpisodePlayer", "Episode found: ${ep.getDisplayTitle()}")
                Log.d("EpisodePlayer", "Video URI: ${ep.videoUri}")
                
                val uri = Uri.parse(ep.videoUri)
                Log.d("EpisodePlayer", "Parsed URI: $uri")
                
                val dataSourceFactory = DefaultDataSource.Factory(context)
                
                val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
                
                Log.d("EpisodePlayer", "Setting media source...")
                exoPlayer.setMediaSource(mediaSource)
                Log.d("EpisodePlayer", "Preparing player...")
                exoPlayer.prepare()
                
                if (ep.lastPosition > 0 && ep.lastPosition < ep.duration) {
                    Log.d("EpisodePlayer", "Seeking to position: ${ep.lastPosition}")
                    exoPlayer.seekTo(ep.lastPosition)
                }
                
                exoPlayer.playWhenReady = true
                Log.d("EpisodePlayer", "Player ready to play")
                isLoading = false
            } catch (e: Exception) {
                Log.e("EpisodePlayer", "Error loading video", e)
                e.printStackTrace()
                hasError = true
                errorMessage = "Erro ao carregar: ${e.message}"
                isLoading = false
            }
        } ?: run {
            Log.e("EpisodePlayer", "Episode not found: $episodeId")
            hasError = true
            errorMessage = "Episódio não encontrado"
            isLoading = false
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
    
    DisposableEffect(isFullscreen) {
        activity?.let {
            if (isFullscreen) {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                WindowCompat.setDecorFitsSystemWindows(it.window, false)
                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        
        onDispose {
            activity?.let {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Log.d("EpisodePlayer", "Playback state changed: $stateName")
                
                if (playbackState == Player.STATE_ENDED) {
                    scope.launch {
                        val duration = exoPlayer.duration
                        seriesManager.updateEpisodeProgress(episodeId, duration, duration)
                    }
                }
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Log.e("EpisodePlayer", "Player error occurred", error)
                Log.e("EpisodePlayer", "Error code: ${error.errorCode}")
                Log.e("EpisodePlayer", "Error message: ${error.message}")
                
                hasError = true
                errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                    PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> 
                        "Não foi possível acessar o vídeo. O acesso pode ter expirado."
                    else -> "Erro ao reproduzir o vídeo: ${error.message}"
                }
                isLoading = false
            }
        }
        exoPlayer.addListener(listener)
        
        onDispose {
            Log.d("EpisodePlayer", "Disposing player...")
            scope.launch {
                val position = exoPlayer.currentPosition
                val duration = exoPlayer.duration
                Log.d("EpisodePlayer", "Saving progress: $position / $duration")
                if (position > 0 && duration > 0) {
                    seriesManager.updateEpisodeProgress(episodeId, position, duration)
                }
            }
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onBackClick()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            hasError -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "O vídeo pode ter sido removido ou o acesso expirou.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Volte para a lista de episódios e re-vincule o vídeo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voltar")
                    }
                }
            }
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            else -> {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            controllerShowTimeoutMs = 3000
                            
                            setFullscreenButtonClickListener { isFullScreen ->
                                isFullscreen = isFullScreen
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        if (!isFullscreen) {
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
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    videoPath: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isFullscreen by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse(videoPath)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    
    DisposableEffect(isFullscreen) {
        activity?.let {
            if (isFullscreen) {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                WindowCompat.setDecorFitsSystemWindows(it.window, false)
                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        
        onDispose {
            activity?.let {
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                val windowInsetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onBackClick()
        }
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
                    
                    setFullscreenButtonClickListener { isFullScreen ->
                        isFullscreen = isFullScreen
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (!isFullscreen) {
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
}

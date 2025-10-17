package com.movie.securevideoapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movie.securevideoapp.VideoEncryptor
import com.movie.securevideoapp.data.SeriesManager
import com.movie.securevideoapp.models.Episode
import com.movie.securevideoapp.models.Season
import com.movie.securevideoapp.models.Series
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeListScreen(
    seasonId: String,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val seriesManager = remember { SeriesManager(context) }
    
    var series by remember { mutableStateOf<Series?>(null) }
    var season by remember { mutableStateOf<Season?>(null) }
    var episodesList by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isEncrypting by remember { mutableStateOf(false) }
    var encryptionProgress by remember { mutableStateOf("") }
    var showPermissionWarning by remember { mutableStateOf(false) }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { videoUri ->
            scope.launch {
                isEncrypting = true
                encryptionProgress = "Adicionando vídeo..."
                try {
                    var persistenceGranted = true
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            videoUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        persistenceGranted = false
                    }
                    
                    val nextNumber = seriesManager.getNextEpisodeNumber(seasonId)
                    val newEpisode = Episode(
                        seasonId = seasonId,
                        number = nextNumber,
                        title = "",
                        videoUri = videoUri.toString()
                    )
                    seriesManager.saveEpisode(newEpisode)
                    episodesList = seriesManager.getEpisodesBySeason(seasonId)
                    encryptionProgress = "Vídeo adicionado com sucesso!"
                    
                    if (!persistenceGranted) {
                        kotlinx.coroutines.delay(1500)
                        showPermissionWarning = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    encryptionProgress = "Erro: ${e.message}"
                } finally {
                    kotlinx.coroutines.delay(1500)
                    isEncrypting = false
                    encryptionProgress = ""
                }
            }
        }
    }
    
    LaunchedEffect(seasonId) {
        isLoading = true
        season = seriesManager.getSeasonById(seasonId)
        season?.let {
            series = seriesManager.getSeriesById(it.seriesId)
        }
        episodesList = seriesManager.getEpisodesBySeason(seasonId)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(series?.title ?: "Carregando...")
                        Text(
                            text = season?.getDisplayTitle() ?: "Carregando...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (!isEncrypting) {
                FloatingActionButton(onClick = { videoPickerLauncher.launch("video/*") }) {
                    Icon(Icons.Filled.Add, "Adicionar episódio")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                isEncrypting -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = encryptionProgress,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                episodesList.isEmpty() -> {
                    EmptyEpisodesState()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(episodesList, key = { it.id }) { episode ->
                            EpisodeCard(
                                episode = episode,
                                onClick = { onNavigateToPlayer(episode.id) },
                                onEdit = {
                                    showAddDialog = true
                                },
                                onDelete = {
                                    scope.launch {
                                        seriesManager.deleteEpisode(episode.id)
                                        episodesList = seriesManager.getEpisodesBySeason(seasonId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showPermissionWarning) {
        AlertDialog(
            onDismissRequest = { showPermissionWarning = false },
            icon = { 
                Icon(
                    Icons.Filled.Info, 
                    null, 
                    tint = MaterialTheme.colorScheme.primary
                ) 
            },
            title = { Text("Vídeo adicionado!") },
            text = { 
                Text(
                    "O vídeo foi adicionado com sucesso.\n\n" +
                    "Nota: O Google Fotos não permite acesso permanente aos vídeos. " +
                    "Se o vídeo parar de funcionar no futuro, basta selecioná-lo novamente. " +
                    "Seu progresso e dados da série serão mantidos."
                ) 
            },
            confirmButton = {
                TextButton(onClick = { showPermissionWarning = false }) {
                    Text("Entendi")
                }
            }
        )
    }
}

@Composable
fun EmptyEpisodesState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.VideoFile,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum episódio adicionado",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque em + para adicionar vídeo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Vídeos do Google Fotos também aparecerão no seletor",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeCard(
    episode: Episode,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.getDisplayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (episode.hasProgress()) {
                    LinearProgressIndicator(
                        progress = { episode.getProgressPercentage() / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${episode.getProgressPercentage()}% assistido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else if (episode.watched) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Assistido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir episódio?") },
            text = { Text("O vídeo será excluído permanentemente. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

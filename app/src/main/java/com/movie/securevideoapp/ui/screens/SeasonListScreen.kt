package com.movie.securevideoapp.ui.screens

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
import com.movie.securevideoapp.data.SeriesManager
import com.movie.securevideoapp.models.Season
import com.movie.securevideoapp.models.Series
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonListScreen(
    seriesId: String,
    onNavigateToEpisodes: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val seriesManager = remember { SeriesManager(context) }
    
    var series by remember { mutableStateOf<Series?>(null) }
    var seasonsList by remember { mutableStateOf<List<Season>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(seriesId) {
        isLoading = true
        series = seriesManager.getSeriesById(seriesId)
        seasonsList = seriesManager.getSeasonsBySeries(seriesId)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(series?.title ?: "Carregando...")
                        Text(
                            text = "Temporadas",
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Adicionar temporada")
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
                seasonsList.isEmpty() -> {
                    EmptySeasonsState()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(seasonsList, key = { it.id }) { season ->
                            SeasonCard(
                                season = season,
                                episodeCount = 0,
                                onClick = { onNavigateToEpisodes(season.id) },
                                onDelete = {
                                    scope.launch {
                                        seriesManager.deleteSeason(season.id)
                                        seasonsList = seriesManager.getSeasonsBySeries(seriesId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddSeasonDialog(
            seriesManager = seriesManager,
            seriesId = seriesId,
            onDismiss = { showAddDialog = false },
            onConfirm = { seasonNumber, seasonTitle ->
                scope.launch {
                    val newSeason = Season(
                        seriesId = seriesId,
                        number = seasonNumber,
                        title = seasonTitle
                    )
                    seriesManager.saveSeason(newSeason)
                    seasonsList = seriesManager.getSeasonsBySeries(seriesId)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun EmptySeasonsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma temporada criada",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque em + para criar uma temporada",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonCard(
    season: Season,
    episodeCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = season.number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = season.getDisplayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$episodeCount episódio${if (episodeCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
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
            title = { Text("Excluir temporada?") },
            text = { Text("Todos os episódios desta temporada serão excluídos. Esta ação não pode ser desfeita.") },
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

@Composable
fun AddSeasonDialog(
    seriesManager: SeriesManager,
    seriesId: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var seasonNumber by remember { mutableStateOf("") }
    var seasonTitle by remember { mutableStateOf("") }
    
    LaunchedEffect(seriesId) {
        val nextNumber = seriesManager.getNextSeasonNumber(seriesId)
        seasonNumber = nextNumber.toString()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Temporada") },
        text = {
            Column {
                OutlinedTextField(
                    value = seasonNumber,
                    onValueChange = { seasonNumber = it.filter { char -> char.isDigit() } },
                    label = { Text("Número*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = seasonTitle,
                    onValueChange = { seasonTitle = it },
                    label = { Text("Título (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val number = seasonNumber.toIntOrNull()
                    if (number != null && number > 0) {
                        onConfirm(number, seasonTitle)
                    }
                },
                enabled = seasonNumber.isNotBlank() && seasonNumber.toIntOrNull() != null && seasonNumber.toInt() > 0
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

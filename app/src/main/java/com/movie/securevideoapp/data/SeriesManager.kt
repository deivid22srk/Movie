package com.movie.securevideoapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.movie.securevideoapp.models.Episode
import com.movie.securevideoapp.models.Season
import com.movie.securevideoapp.models.Series
import com.movie.securevideoapp.models.WatchProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SeriesManager(private val context: Context) {
    private val gson = Gson()
    private val seriesFile = File(context.filesDir, "series.json")
    private val seasonsFile = File(context.filesDir, "seasons.json")
    private val episodesFile = File(context.filesDir, "episodes.json")
    private val progressFile = File(context.filesDir, "progress.json")
    
    suspend fun getAllSeries(): List<Series> = withContext(Dispatchers.IO) {
        if (!seriesFile.exists()) return@withContext emptyList()
        try {
            val json = seriesFile.readText()
            val type = object : TypeToken<List<Series>>() {}.type
            gson.fromJson<List<Series>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun saveSeries(series: Series) = withContext(Dispatchers.IO) {
        val allSeries = getAllSeries().toMutableList()
        val index = allSeries.indexOfFirst { it.id == series.id }
        if (index >= 0) {
            allSeries[index] = series
        } else {
            allSeries.add(series)
        }
        seriesFile.writeText(gson.toJson(allSeries))
    }
    
    suspend fun deleteSeries(seriesId: String) = withContext(Dispatchers.IO) {
        val allSeries = getAllSeries().toMutableList()
        allSeries.removeAll { it.id == seriesId }
        seriesFile.writeText(gson.toJson(allSeries))
        
        val seasons = getSeasonsBySeries(seriesId)
        seasons.forEach { season ->
            val episodes = getEpisodesBySeason(season.id)
            episodes.forEach { episode ->
                deleteEpisode(episode.id)
            }
            deleteSeason(season.id)
        }
    }
    
    suspend fun getSeasonsBySeries(seriesId: String): List<Season> = withContext(Dispatchers.IO) {
        if (!seasonsFile.exists()) return@withContext emptyList()
        try {
            val json = seasonsFile.readText()
            val type = object : TypeToken<List<Season>>() {}.type
            val allSeasons = gson.fromJson<List<Season>>(json, type) ?: emptyList()
            allSeasons.filter { it.seriesId == seriesId }.sortedBy { it.number }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun saveSeason(season: Season) = withContext(Dispatchers.IO) {
        val allSeasons = if (seasonsFile.exists()) {
            val json = seasonsFile.readText()
            val type = object : TypeToken<List<Season>>() {}.type
            gson.fromJson<List<Season>>(json, type)?.toMutableList() ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        val index = allSeasons.indexOfFirst { it.id == season.id }
        if (index >= 0) {
            allSeasons[index] = season
        } else {
            allSeasons.add(season)
        }
        seasonsFile.writeText(gson.toJson(allSeasons))
    }
    
    suspend fun deleteSeason(seasonId: String) = withContext(Dispatchers.IO) {
        if (!seasonsFile.exists()) return@withContext
        val json = seasonsFile.readText()
        val type = object : TypeToken<List<Season>>() {}.type
        val allSeasons = gson.fromJson<List<Season>>(json, type)?.toMutableList() ?: return@withContext
        allSeasons.removeAll { it.id == seasonId }
        seasonsFile.writeText(gson.toJson(allSeasons))
        
        val episodes = getEpisodesBySeason(seasonId)
        episodes.forEach { deleteEpisode(it.id) }
    }
    
    suspend fun getEpisodesBySeason(seasonId: String): List<Episode> = withContext(Dispatchers.IO) {
        if (!episodesFile.exists()) return@withContext emptyList()
        try {
            val json = episodesFile.readText()
            val type = object : TypeToken<List<Episode>>() {}.type
            val allEpisodes = gson.fromJson<List<Episode>>(json, type) ?: emptyList()
            allEpisodes.filter { it.seasonId == seasonId }.sortedBy { it.number }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun saveEpisode(episode: Episode) = withContext(Dispatchers.IO) {
        val allEpisodes = if (episodesFile.exists()) {
            val json = episodesFile.readText()
            val type = object : TypeToken<List<Episode>>() {}.type
            gson.fromJson<List<Episode>>(json, type)?.toMutableList() ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        val index = allEpisodes.indexOfFirst { it.id == episode.id }
        if (index >= 0) {
            allEpisodes[index] = episode
        } else {
            allEpisodes.add(episode)
        }
        episodesFile.writeText(gson.toJson(allEpisodes))
    }
    
    suspend fun deleteEpisode(episodeId: String) = withContext(Dispatchers.IO) {
        if (!episodesFile.exists()) return@withContext
        val json = episodesFile.readText()
        val type = object : TypeToken<List<Episode>>() {}.type
        val allEpisodes = gson.fromJson<List<Episode>>(json, type)?.toMutableList() ?: return@withContext
        
        allEpisodes.removeAll { it.id == episodeId }
        episodesFile.writeText(gson.toJson(allEpisodes))
    }
    
    suspend fun updateEpisodeProgress(episodeId: String, position: Long, duration: Long) = withContext(Dispatchers.IO) {
        if (!episodesFile.exists()) return@withContext
        val json = episodesFile.readText()
        val type = object : TypeToken<List<Episode>>() {}.type
        val allEpisodes = gson.fromJson<List<Episode>>(json, type)?.toMutableList() ?: return@withContext
        
        val index = allEpisodes.indexOfFirst { it.id == episodeId }
        if (index >= 0) {
            val episode = allEpisodes[index]
            val watched = position >= duration * 0.95
            allEpisodes[index] = episode.copy(
                lastPosition = position,
                duration = duration,
                watched = watched
            )
            episodesFile.writeText(gson.toJson(allEpisodes))
        }
    }
    
    suspend fun getNextSeasonNumber(seriesId: String): Int {
        val seasons = getSeasonsBySeries(seriesId)
        return if (seasons.isEmpty()) 1 else seasons.maxOf { it.number } + 1
    }
    
    suspend fun getNextEpisodeNumber(seasonId: String): Int {
        val episodes = getEpisodesBySeason(seasonId)
        return if (episodes.isEmpty()) 1 else episodes.maxOf { it.number } + 1
    }
    
    suspend fun getSeriesById(seriesId: String): Series? {
        return getAllSeries().find { it.id == seriesId }
    }
    
    suspend fun getSeasonById(seasonId: String): Season? = withContext(Dispatchers.IO) {
        if (!seasonsFile.exists()) return@withContext null
        val json = seasonsFile.readText()
        val type = object : TypeToken<List<Season>>() {}.type
        val allSeasons = gson.fromJson<List<Season>>(json, type) ?: return@withContext null
        allSeasons.find { it.id == seasonId }
    }
    
    suspend fun getEpisodeById(episodeId: String): Episode? = withContext(Dispatchers.IO) {
        if (!episodesFile.exists()) return@withContext null
        val json = episodesFile.readText()
        val type = object : TypeToken<List<Episode>>() {}.type
        val allEpisodes = gson.fromJson<List<Episode>>(json, type) ?: return@withContext null
        allEpisodes.find { it.id == episodeId }
    }
    
    suspend fun saveWatchProgress(progress: WatchProgress) = withContext(Dispatchers.IO) {
        val allProgress = if (progressFile.exists()) {
            val json = progressFile.readText()
            val type = object : TypeToken<List<WatchProgress>>() {}.type
            gson.fromJson<List<WatchProgress>>(json, type)?.toMutableList() ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        allProgress.removeAll { it.episodeId == progress.episodeId }
        allProgress.add(progress)
        progressFile.writeText(gson.toJson(allProgress))
    }
    
    suspend fun getWatchProgress(episodeId: String): WatchProgress? = withContext(Dispatchers.IO) {
        if (!progressFile.exists()) return@withContext null
        val json = progressFile.readText()
        val type = object : TypeToken<List<WatchProgress>>() {}.type
        val allProgress = gson.fromJson<List<WatchProgress>>(json, type) ?: return@withContext null
        allProgress.find { it.episodeId == episodeId }
    }
}

package com.movie.securevideoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.movie.securevideoapp.ui.screens.*
import com.movie.securevideoapp.ui.theme.SecureVideoAppTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SecureVideoAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToPlayer = { videoPath ->
                    val encodedPath = URLEncoder.encode(videoPath, StandardCharsets.UTF_8.toString())
                    navController.navigate("player/$encodedPath")
                },
                onNavigateToSeries = {
                    navController.navigate("series")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("series") {
            SeriesListScreen(
                onNavigateToSeasons = { seriesId ->
                    navController.navigate("seasons/$seriesId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "seasons/{seriesId}",
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
            SeasonListScreen(
                seriesId = seriesId,
                onNavigateToEpisodes = { seasonId ->
                    navController.navigate("episodes/$seasonId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "episodes/{seasonId}",
            arguments = listOf(
                navArgument("seasonId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seasonId = backStackEntry.arguments?.getString("seasonId") ?: ""
            EpisodeListScreen(
                seasonId = seasonId,
                onNavigateToPlayer = { episodeId ->
                    navController.navigate("episodePlayer/$episodeId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "episodePlayer/{episodeId}",
            arguments = listOf(
                navArgument("episodeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
            EpisodePlayerScreen(
                episodeId = episodeId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "player/{videoPath}",
            arguments = listOf(
                navArgument("videoPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("videoPath") ?: ""
            val videoPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
            
            PlayerScreen(
                videoPath = videoPath,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

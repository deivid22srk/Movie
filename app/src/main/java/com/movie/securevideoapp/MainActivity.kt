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
import com.movie.securevideoapp.ui.screens.HomeScreen
import com.movie.securevideoapp.ui.screens.PlayerScreen
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
                }
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

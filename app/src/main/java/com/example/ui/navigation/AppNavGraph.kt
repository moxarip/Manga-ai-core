package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MangaDetailScreen
import com.example.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier
    ) {
        composable<Home> {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToMangaDetail = { sourceId, url -> 
                    navController.navigate(MangaDetail(sourceId, url)) 
                }
            )
        }
        
        composable<Settings> {
            SettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        composable<MangaDetail> { backStackEntry ->
            val detail: MangaDetail = backStackEntry.toRoute()
            MangaDetailScreen(
                sourceId = detail.sourceId,
                mangaUrl = detail.mangaUrl,
                onBack = { navController.navigateUp() }
            )
        }
    }
}

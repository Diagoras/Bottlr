package com.bottlr.app.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bottlr.app.ui.components.NavDestination
import com.bottlr.app.ui.details.BottleDetailsScreen
import com.bottlr.app.ui.details.CocktailDetailsScreen
import com.bottlr.app.ui.editor.BottleEditorScreen
import com.bottlr.app.ui.editor.CocktailEditorScreen
import com.bottlr.app.ui.gallery.BottleGalleryScreen
import com.bottlr.app.ui.gallery.CocktailGalleryScreen
import com.bottlr.app.ui.home.HomeScreen
import com.bottlr.app.ui.settings.ApiKeySettingsScreen
import com.bottlr.app.ui.settings.SettingsScreen
import com.bottlr.app.ui.smartcapture.CaptureReviewScreen
import com.bottlr.app.ui.smartcapture.SmartCaptureScreen
import android.net.Uri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun BottlrNavHost(
    navController: NavHostController = rememberNavController()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                drawerState = drawerState,
                onNavigate = { destination ->
                    when (destination) {
                        NavDestination.Home -> { /* Already here */ }
                        NavDestination.LiquorCabinet -> navController.navigate(BottleGallery)
                        NavDestination.Cocktails -> navController.navigate(CocktailGallery)
                        NavDestination.Settings -> navController.navigate(Settings)
                    }
                },
                onNavigateToBottleGallery = { navController.navigate(BottleGallery) },
                onNavigateToCocktailGallery = { navController.navigate(CocktailGallery) },
                onNavigateToSettings = { navController.navigate(Settings) },
                onAddBottle = { navController.navigate(BottleEditor()) },
                onAddCocktail = { navController.navigate(CocktailEditor()) },
                onSmartAdd = { navController.navigate(SmartCapture) }
            )
        }

        composable<BottleGallery> {
            BottleGalleryScreen(
                drawerState = drawerState,
                onNavigate = { destination ->
                    when (destination) {
                        NavDestination.Home -> navController.navigate(Home) {
                            popUpTo(Home) { inclusive = true }
                        }
                        NavDestination.LiquorCabinet -> { /* Already here */ }
                        NavDestination.Cocktails -> navController.navigate(CocktailGallery)
                        NavDestination.Settings -> navController.navigate(Settings)
                    }
                },
                onBottleClick = { bottleId -> navController.navigate(BottleDetails(bottleId)) },
                onAddBottle = { navController.navigate(BottleEditor()) }
            )
        }

        composable<CocktailGallery> {
            CocktailGalleryScreen(
                drawerState = drawerState,
                onNavigate = { destination ->
                    when (destination) {
                        NavDestination.Home -> navController.navigate(Home) {
                            popUpTo(Home) { inclusive = true }
                        }
                        NavDestination.LiquorCabinet -> navController.navigate(BottleGallery)
                        NavDestination.Cocktails -> { /* Already here */ }
                        NavDestination.Settings -> navController.navigate(Settings)
                    }
                },
                onCocktailClick = { cocktailId -> navController.navigate(CocktailDetails(cocktailId)) },
                onAddCocktail = { navController.navigate(CocktailEditor()) }
            )
        }

        composable<BottleDetails> {
            val route = it.toRoute<BottleDetails>()
            BottleDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditBottle = { bottleId -> navController.navigate(BottleEditor(bottleId)) }
            )
        }

        composable<CocktailDetails> {
            val route = it.toRoute<CocktailDetails>()
            CocktailDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditCocktail = { cocktailId -> navController.navigate(CocktailEditor(cocktailId)) }
            )
        }

        composable<BottleEditor> {
            BottleEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CocktailEditor> {
            CocktailEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToApiKeys = { navController.navigate(ApiKeySettings) }
            )
        }

        composable<ApiKeySettings> {
            ApiKeySettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SmartCapture> {
            SmartCaptureScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReview = { photoUri ->
                    navController.navigate(CaptureReview(photoUri.toString()))
                },
                onNavigateToApiKeys = { navController.navigate(ApiKeySettings) }
            )
        }

        composable<CaptureReview> {
            val route = it.toRoute<CaptureReview>()
            CaptureReviewScreen(
                photoUri = Uri.parse(route.photoUri),
                onNavigateBack = { navController.popBackStack() },
                onConfirm = { bottle, photoUri ->
                    // Navigate to editor with pre-filled data
                    val prefillJson = Json.encodeToString(bottle)
                    navController.navigate(BottleEditor(prefillJson = prefillJson)) {
                        // Pop up to gallery when going to editor
                        popUpTo(SmartCapture) { inclusive = true }
                    }
                },
                onRetake = {
                    navController.popBackStack()
                }
            )
        }
    }
}

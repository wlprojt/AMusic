package com.example.music


import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(viewModel: PlayerViewModel) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 1000)
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 1000)
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down
            )
        }
    ) {
        composable("home") {
            PlayerScreen( navController, viewModel)
        }
        composable("play") {
            MusicPlayerScreen(navController, viewModel)
        }
    }
}
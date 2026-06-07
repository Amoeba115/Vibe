package me.ayra.music.ui.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn

private const val NAV_DUR = 280

@OptIn(ExperimentalAnimationApi::class)
fun <S> AnimatedContentTransitionScope<S>.modernEnter(): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(NAV_DUR),
    ) + fadeIn(animationSpec = tween(NAV_DUR)) +
        scaleIn(
            initialScale = 0.98f,
            animationSpec = tween(NAV_DUR),
        )

@OptIn(ExperimentalAnimationApi::class)
fun <S> AnimatedContentTransitionScope<S>.modernExit(): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(NAV_DUR),
    ) + fadeOut(animationSpec = tween(NAV_DUR))

@OptIn(ExperimentalAnimationApi::class)
fun <S> AnimatedContentTransitionScope<S>.modernPopEnter(): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(NAV_DUR),
    ) + fadeIn(animationSpec = tween(NAV_DUR)) +
        scaleIn(
            initialScale = 0.99f,
            animationSpec = tween(NAV_DUR),
        )

@OptIn(ExperimentalAnimationApi::class)
fun <S> AnimatedContentTransitionScope<S>.modernPopExit(): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(NAV_DUR),
    ) + fadeOut(animationSpec = tween(NAV_DUR))

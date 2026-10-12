package com.zotmacros.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/** Finite Compose animations inherit Android's animator duration scale, including zero. */
object ZotMotion {
    private val tabs = listOf("Today", "Menu", "History", "Settings")
    private fun direction(from: String?, to: String?) =
        if (tabs.indexOf(to) >= tabs.indexOf(from)) 1 else -1

    fun tabEnter(from: String?, to: String?): EnterTransition =
        fadeIn(tween(240)) + slideInHorizontally(
            tween(300, easing = FastOutSlowInEasing),
            initialOffsetX = { width -> direction(from, to) * width / 8 })

    fun tabExit(from: String?, to: String?): ExitTransition =
        fadeOut(tween(180)) + slideOutHorizontally(
            tween(300, easing = FastOutSlowInEasing),
            targetOffsetX = { width -> -direction(from, to) * width / 8 })

    fun progress() = tween<Float>(durationMillis = 280, easing = FastOutSlowInEasing)
}

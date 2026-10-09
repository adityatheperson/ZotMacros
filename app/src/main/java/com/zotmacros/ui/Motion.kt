package com.zotmacros.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/** Finite Compose animations inherit Android's animator duration scale, including zero. */
object ZotMotion {
    const val EnterMillis = 180
    const val ExitMillis = 120
    fun progress() = tween<Float>(durationMillis = 280, easing = FastOutSlowInEasing)
}

package com.lowerbackstretching

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bridges activity-level PiP callbacks with Compose so the player can render a compact layout in PiP. */
class PictureInPictureHost {

    /** True while the player is on screen and eligible for PiP. */
    val pipEligible = MutableStateFlow(false)

    private val _inPip = MutableStateFlow(false)
    /** True while the system has placed us in PiP mode. */
    val inPip: StateFlow<Boolean> = _inPip.asStateFlow()

    fun setInPip(value: Boolean) { _inPip.value = value }
}

val LocalPictureInPictureHost = compositionLocalOf<PictureInPictureHost> {
    error("LocalPictureInPictureHost not provided")
}

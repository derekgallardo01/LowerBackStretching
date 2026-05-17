package com.lowerbackstretching

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The single source of truth for Picture-in-Picture state, bridging
 * the activity (which owns the OS-level PiP transitions) with Compose
 * (which renders different layouts in PiP). The activity creates one
 * instance, exposes it through [LocalPictureInPictureHost], and reads
 * back the same instance to decide whether to call
 * `enterPictureInPictureMode()` on `onUserLeaveHint`.
 */
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

package com.lowerbackstretching.ui.safety

/**
 * The five red-flag symptoms shown during the onboarding safety check
 * and the Settings advisory. These are the highest-severity signals
 * that warrant clinician evaluation before exercise: cauda equina,
 * radiculopathy, fracture, and infection. Wording is plain-language so
 * a non-clinician can self-recognize without medical jargon.
 *
 * Intentionally not exhaustive — cancer history, unexplained weight
 * loss, and night pain matter clinically but are harder to phrase
 * non-scarily for a first onboarding pass. Defer to a future expansion.
 */
data class RedFlag(val short: String)

internal val redFlags = listOf(
    RedFlag("Numbness or weakness in your legs or feet"),
    RedFlag("Pain shooting down one or both legs, below the knee"),
    RedFlag("Loss of control over your bladder or bowels"),
    RedFlag("Severe back pain after a fall or accident"),
    RedFlag("Fever along with your back pain"),
)

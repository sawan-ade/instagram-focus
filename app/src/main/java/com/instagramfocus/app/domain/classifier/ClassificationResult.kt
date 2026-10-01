package com.instagramfocus.app.domain.classifier

import com.instagramfocus.app.domain.model.ScreenType

/**
 * Detailed outcome of classifying an on-screen state.
 */
data class ClassificationResult(
    val screenType: ScreenType,
    val confidence: Float,
    val matchedRules: List<String>,
    val detectedSignals: List<String> = emptyList()
) {
    companion object {
        fun unknown(reason: String = "No confident signals matched"): ClassificationResult {
            return ClassificationResult(
                screenType = ScreenType.UNKNOWN,
                confidence = 0.0f,
                matchedRules = listOf("FALLBACK: $reason")
            )
        }
    }
}

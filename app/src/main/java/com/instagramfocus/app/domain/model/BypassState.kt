package com.instagramfocus.app.domain.model

/**
 * Represents the temporary emergency bypass state.
 */
data class BypassState(
    val isActive: Boolean = false,
    val expiresAtTimestamp: Long = 0L,
    val totalDurationMinutes: Int = 0
) {
    val remainingSeconds: Long
        get() {
            if (!isActive) return 0L
            val now = System.currentTimeMillis()
            val diff = (expiresAtTimestamp - now) / 1000L
            return if (diff > 0) diff else 0L
        }

    val isExpired: Boolean
        get() = isActive && System.currentTimeMillis() >= expiresAtTimestamp
}

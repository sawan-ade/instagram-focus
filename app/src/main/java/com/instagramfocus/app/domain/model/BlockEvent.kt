package com.instagramfocus.app.domain.model

/**
 * Event recorded whenever an Instagram distraction surface is blocked.
 */
data class BlockEvent(
    val id: Long = 0,
    val screenType: ScreenType,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = "",
    val matchedRule: String = ""
)

/**
 * Aggregated statistics for the user dashboard and statistics view.
 */
data class StatisticsSummary(
    val reelsBlockedToday: Int = 0,
    val exploreBlockedToday: Int = 0,
    val feedBlockedToday: Int = 0,
    val totalBlockedToday: Int = 0,
    val weeklyBlockedTotal: Int = 0,
    val instagramSessionsCount: Int = 0,
    val dmSessionsCount: Int = 0
)

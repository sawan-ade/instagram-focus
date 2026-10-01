package com.instagramfocus.app.domain.model

/**
 * High-level classification of surfaces inside the Instagram application.
 */
enum class ScreenType(val displayName: String, val isDistractionSurface: Boolean) {
    HOME_FEED("Home Feed", isDistractionSurface = true),
    REELS("Reels", isDistractionSurface = true),
    EXPLORE("Explore & Search", isDistractionSurface = true),
    STORY("Stories", isDistractionSurface = false),
    DM_INBOX("Direct Messages", isDistractionSurface = false),
    DM_CONVERSATION("Direct Chat", isDistractionSurface = false),
    PROFILE("Profile", isDistractionSurface = false),
    FOLLOW_REQUESTS("Follow Requests", isDistractionSurface = false),
    NOTIFICATIONS("Activity & Notifications", isDistractionSurface = false),
    UNKNOWN("Unknown / Other", isDistractionSurface = false)
}

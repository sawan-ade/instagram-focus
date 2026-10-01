package com.instagramfocus.app.domain.model

/**
 * User configuration preferences for Instagram Focus Mode.
 */
data class FocusSettings(
    val isFocusModeEnabled: Boolean = true,
    
    // Blocked Surfaces
    val blockReels: Boolean = true,
    val blockExplore: Boolean = true,
    val blockFeed: Boolean = true,
    val blockSuggestedContent: Boolean = true,
    
    // Allowed Surfaces
    val allowDms: Boolean = true,
    val allowStories: Boolean = true,
    val allowFollowRequests: Boolean = true,
    val allowProfiles: Boolean = true,
    val allowNotifications: Boolean = true,
    
    // Safety & Friction
    val isStrictModeEnabled: Boolean = false,
    val enableFrictionSurvey: Boolean = true,
    val bypassDurationMinutes: Int = 10,
    val hasCompletedOnboarding: Boolean = false
)

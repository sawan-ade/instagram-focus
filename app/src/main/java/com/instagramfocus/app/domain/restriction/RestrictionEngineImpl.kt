package com.instagramfocus.app.domain.restriction

import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.domain.model.ScreenType

/**
 * Standard implementation of RestrictionEngine ensuring safe fallbacks
 * and strictly respecting user whitelist settings.
 */
class RestrictionEngineImpl : RestrictionEngine {

    override fun evaluate(
        classification: ClassificationResult,
        settings: FocusSettings,
        bypassState: BypassState
    ): RestrictionDecision {
        // 1. If Focus Mode is globally OFF, always allow
        if (!settings.isFocusModeEnabled) {
            return RestrictionDecision.Allow("Focus Mode is turned OFF")
        }

        // 2. If temporary emergency bypass is active, allow
        if (bypassState.isActive && !bypassState.isExpired) {
            return RestrictionDecision.Allow(
                "Temporary bypass active (${bypassState.remainingSeconds}s remaining)"
            )
        }

        val rule = classification.matchedRules.firstOrNull() ?: "ClassificationRule"

        return when (classification.screenType) {
            ScreenType.REELS -> {
                if (settings.blockReels) {
                    RestrictionDecision.Block(
                        screenType = ScreenType.REELS,
                        reason = "Reels are disabled during Focus Mode.",
                        ruleMatched = rule
                    )
                } else {
                    RestrictionDecision.Allow("Reels blocking disabled in Settings")
                }
            }

            ScreenType.EXPLORE -> {
                if (settings.blockExplore) {
                    RestrictionDecision.Block(
                        screenType = ScreenType.EXPLORE,
                        reason = "Explore grid is disabled during Focus Mode.",
                        ruleMatched = rule
                    )
                } else {
                    RestrictionDecision.Allow("Explore blocking disabled in Settings")
                }
            }

            ScreenType.HOME_FEED -> {
                if (settings.blockFeed) {
                    RestrictionDecision.Block(
                        screenType = ScreenType.HOME_FEED,
                        reason = "Endless Home Feed is disabled during Focus Mode.",
                        ruleMatched = rule
                    )
                } else {
                    RestrictionDecision.Allow("Feed blocking disabled in Settings")
                }
            }

            ScreenType.DM_INBOX, ScreenType.DM_CONVERSATION -> {
                if (settings.allowDms) {
                    RestrictionDecision.Allow("Direct Messages are allowed for intentional communication.")
                } else {
                    RestrictionDecision.Block(
                        screenType = classification.screenType,
                        reason = "Direct Messages are restricted in custom settings.",
                        ruleMatched = rule
                    )
                }
            }

            ScreenType.STORY -> {
                if (settings.allowStories) {
                    RestrictionDecision.Allow("Stories are allowed.")
                } else {
                    RestrictionDecision.Block(
                        screenType = ScreenType.STORY,
                        reason = "Stories are restricted in custom settings.",
                        ruleMatched = rule
                    )
                }
            }

            ScreenType.FOLLOW_REQUESTS -> {
                if (settings.allowFollowRequests) {
                    RestrictionDecision.Allow("Follow requests are allowed.")
                } else {
                    RestrictionDecision.Block(
                        screenType = ScreenType.FOLLOW_REQUESTS,
                        reason = "Follow requests restricted.",
                        ruleMatched = rule
                    )
                }
            }

            ScreenType.NOTIFICATIONS -> {
                if (settings.allowNotifications) {
                    RestrictionDecision.Allow("Activity & social notifications are allowed.")
                } else {
                    RestrictionDecision.Block(
                        screenType = ScreenType.NOTIFICATIONS,
                        reason = "Notifications restricted.",
                        ruleMatched = rule
                    )
                }
            }

            ScreenType.PROFILE -> {
                if (settings.allowProfiles) {
                    RestrictionDecision.Allow("User profile browsing is allowed.")
                } else {
                    RestrictionDecision.Block(
                        screenType = ScreenType.PROFILE,
                        reason = "Profiles restricted.",
                        ruleMatched = rule
                    )
                }
            }

            ScreenType.UNKNOWN -> {
                // Safe Fallback Rule:
                // If the classifier is uncertain, DO NOT aggressively block the user.
                // UNKNOWN -> ALLOW or small warning, rather than destroying legitimate Instagram functionality.
                if (settings.isStrictModeEnabled) {
                    RestrictionDecision.Warn(
                        screenType = ScreenType.UNKNOWN,
                        message = "Unidentified screen: Strict Mode is monitoring."
                    )
                } else {
                    RestrictionDecision.Allow(
                        "Safe fallback: Unrecognized screen permitted to protect communication."
                    )
                }
            }
        }
    }
}

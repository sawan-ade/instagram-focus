package com.instagramfocus.app.domain.model

/**
 * The decision rendered by RestrictionEngine for a classified Instagram screen.
 */
sealed class RestrictionDecision {
    data class Allow(val reason: String) : RestrictionDecision()
    data class Block(val screenType: ScreenType, val reason: String, val ruleMatched: String) : RestrictionDecision()
    data class Warn(val screenType: ScreenType, val message: String) : RestrictionDecision()

    val isBlocked: Boolean get() = this is Block
}

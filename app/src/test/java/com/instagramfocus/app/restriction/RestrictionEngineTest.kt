package com.instagramfocus.app.restriction

import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.domain.restriction.RestrictionEngineImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RestrictionEngineTest {

    private lateinit var engine: RestrictionEngineImpl
    private val defaultSettings = FocusSettings(isFocusModeEnabled = true)
    private val inactiveBypass = BypassState(isActive = false)

    @Before
    fun setUp() {
        engine = RestrictionEngineImpl()
    }

    // Test Case 1: Open Instagram -> Feed -> BLOCK
    @Test
    fun testFeedIsBlockedWhenFocusOn() {
        val classification = ClassificationResult(ScreenType.HOME_FEED, 0.90f, listOf("HomeFeedRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Block)
        assertEquals(ScreenType.HOME_FEED, (decision as RestrictionDecision.Block).screenType)
    }

    // Test Case 2: Open Reels -> BLOCK
    @Test
    fun testReelsIsBlockedWhenFocusOn() {
        val classification = ClassificationResult(ScreenType.REELS, 0.95f, listOf("ReelsRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Block)
        assertEquals(ScreenType.REELS, (decision as RestrictionDecision.Block).screenType)
    }

    // Test Case 3: Open Explore -> BLOCK
    @Test
    fun testExploreIsBlockedWhenFocusOn() {
        val classification = ClassificationResult(ScreenType.EXPLORE, 0.92f, listOf("ExploreRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Block)
        assertEquals(ScreenType.EXPLORE, (decision as RestrictionDecision.Block).screenType)
    }

    // Test Case 4: Open DM inbox -> ALLOW
    @Test
    fun testDmInboxIsAllowed() {
        val classification = ClassificationResult(ScreenType.DM_INBOX, 0.94f, listOf("DmInboxRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Allow)
    }

    // Test Case 5: Open conversation -> ALLOW
    @Test
    fun testDmConversationIsAllowed() {
        val classification = ClassificationResult(ScreenType.DM_CONVERSATION, 0.95f, listOf("DmConversationRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Allow)
    }

    // Test Case 6: Open Story -> ALLOW
    @Test
    fun testStoryIsAllowed() {
        val classification = ClassificationResult(ScreenType.STORY, 0.95f, listOf("StoryRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Allow)
    }

    // Test Case 7: Open Follow Requests -> ALLOW
    @Test
    fun testFollowRequestsIsAllowed() {
        val classification = ClassificationResult(ScreenType.FOLLOW_REQUESTS, 0.95f, listOf("FollowRequestsRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Allow)
    }

    // Test Case 8: Open Profile -> ALLOW
    @Test
    fun testProfileIsAllowed() {
        val classification = ClassificationResult(ScreenType.PROFILE, 0.90f, listOf("ProfileRule"))
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Allow)
    }

    // Test Case 9: Unknown screen -> SAFE FALLBACK (ALLOW)
    @Test
    fun testUnknownScreenSafeFallbackAllows() {
        val classification = ClassificationResult.unknown("Ambiguous signals")
        val decision = engine.evaluate(classification, defaultSettings, inactiveBypass)

        // Safe Fallback Rule: must NOT aggressively block
        assertTrue(decision is RestrictionDecision.Allow)
    }

    // Test Case 10: Focus Mode OFF -> no intervention
    @Test
    fun testFocusModeOffAllowsAllDistractions() {
        val disabledSettings = FocusSettings(isFocusModeEnabled = false)

        val reelsClassification = ClassificationResult(ScreenType.REELS, 0.99f, listOf("ReelsRule"))
        val decision = engine.evaluate(reelsClassification, disabledSettings, inactiveBypass)

        assertTrue(decision is RestrictionDecision.Allow)
        assertEquals("Focus Mode is turned OFF", (decision as RestrictionDecision.Allow).reason)
    }

    // Test Case 11: Temporary bypass -> restrictions paused
    @Test
    fun testActiveTemporaryBypassPausesRestrictions() {
        val activeBypass = BypassState(
            isActive = true,
            expiresAtTimestamp = System.currentTimeMillis() + 600_000L, // 10 minutes in future
            totalDurationMinutes = 10
        )

        val reelsClassification = ClassificationResult(ScreenType.REELS, 0.99f, listOf("ReelsRule"))
        val decision = engine.evaluate(reelsClassification, defaultSettings, activeBypass)

        assertTrue(decision is RestrictionDecision.Allow)
        assertTrue((decision as RestrictionDecision.Allow).reason.contains("Temporary bypass active"))
    }

    // Test Case 12: Timer expires -> restrictions return
    @Test
    fun testExpiredTemporaryBypassRestoresRestrictions() {
        val expiredBypass = BypassState(
            isActive = true,
            expiresAtTimestamp = System.currentTimeMillis() - 1000L, // Expired 1 second ago
            totalDurationMinutes = 10
        )

        val reelsClassification = ClassificationResult(ScreenType.REELS, 0.99f, listOf("ReelsRule"))
        val decision = engine.evaluate(reelsClassification, defaultSettings, expiredBypass)

        // Restrictions have returned!
        assertTrue(decision is RestrictionDecision.Block)
        assertEquals(ScreenType.REELS, (decision as RestrictionDecision.Block).screenType)
    }
}

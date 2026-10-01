package com.instagramfocus.app.classifier

import com.instagramfocus.app.domain.classifier.NodeSnapshot
import com.instagramfocus.app.domain.classifier.ScreenClassifierImpl
import com.instagramfocus.app.domain.model.ScreenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScreenClassifierTest {

    private lateinit var classifier: ScreenClassifierImpl

    @Before
    fun setUp() {
        classifier = ScreenClassifierImpl()
    }

    @Test
    fun testReelsScreenDetection() {
        val reelsNode = NodeSnapshot(
            text = "Reels",
            contentDescription = "Reel by user",
            viewIdResourceName = "com.instagram.android:id/clips_viewer_view_pager",
            children = listOf(
                NodeSnapshot(text = "Original audio", contentDescription = "Audio used in this reel"),
                NodeSnapshot(contentDescription = "Remix this reel")
            )
        )

        val result = classifier.classify(reelsNode, null)

        assertEquals(ScreenType.REELS, result.screenType)
        assertTrue(result.confidence > 0.85f)
        assertTrue(result.screenType.isDistractionSurface)
    }

    @Test
    fun testExploreGridDetection() {
        val exploreNode = NodeSnapshot(
            contentDescription = "Search and explore",
            viewIdResourceName = "com.instagram.android:id/explore_grid",
            children = listOf(
                NodeSnapshot(text = "Search instagram")
            )
        )

        val result = classifier.classify(exploreNode, null)

        assertEquals(ScreenType.EXPLORE, result.screenType)
        assertTrue(result.confidence > 0.80f)
        assertTrue(result.screenType.isDistractionSurface)
    }

    @Test
    fun testHomeFeedDetection() {
        val feedNode = NodeSnapshot(
            text = "Home",
            viewIdResourceName = "com.instagram.android:id/feed_tab",
            children = listOf(
                NodeSnapshot(text = "Suggested for you"),
                NodeSnapshot(text = "You're all caught up")
            )
        )

        val result = classifier.classify(feedNode, null)

        assertEquals(ScreenType.HOME_FEED, result.screenType)
        assertTrue(result.screenType.isDistractionSurface)
    }

    @Test
    fun testDmInboxDetection() {
        val dmInboxNode = NodeSnapshot(
            text = "Messages",
            viewIdResourceName = "com.instagram.android:id/direct_inbox",
            children = listOf(
                NodeSnapshot(text = "Primary"),
                NodeSnapshot(text = "General"),
                NodeSnapshot(text = "Requests")
            )
        )

        val result = classifier.classify(dmInboxNode, null)

        assertEquals(ScreenType.DM_INBOX, result.screenType)
        assertTrue(!result.screenType.isDistractionSurface)
    }

    @Test
    fun testDmConversationDetection() {
        val chatNode = NodeSnapshot(
            text = "Message...",
            viewIdResourceName = "com.instagram.android:id/row_thread_composer_edittext",
            children = listOf(
                NodeSnapshot(contentDescription = "Send message"),
                NodeSnapshot(contentDescription = "Direct camera")
            )
        )

        val result = classifier.classify(chatNode, null)

        assertEquals(ScreenType.DM_CONVERSATION, result.screenType)
        assertTrue(!result.screenType.isDistractionSurface)
    }

    @Test
    fun testStoryViewerDetection() {
        val storyNode = NodeSnapshot(
            text = "Reply to alex...",
            viewIdResourceName = "com.instagram.android:id/reel_viewer_progress_bar",
            children = listOf(
                NodeSnapshot(contentDescription = "Like story")
            )
        )

        val result = classifier.classify(storyNode, null)

        assertEquals(ScreenType.STORY, result.screenType)
        assertTrue(!result.screenType.isDistractionSurface)
    }

    @Test
    fun testFollowRequestsDetection() {
        val requestNode = NodeSnapshot(
            text = "Follow requests",
            children = listOf(
                NodeSnapshot(text = "Confirm request"),
                NodeSnapshot(text = "Delete request")
            )
        )

        val result = classifier.classify(requestNode, null)

        assertEquals(ScreenType.FOLLOW_REQUESTS, result.screenType)
        assertTrue(!result.screenType.isDistractionSurface)
    }

    @Test
    fun testUserProfileDetection() {
        val profileNode = NodeSnapshot(
            viewIdResourceName = "com.instagram.android:id/profile_tab",
            children = listOf(
                NodeSnapshot(text = "Posts"),
                NodeSnapshot(text = "Followers"),
                NodeSnapshot(text = "Following"),
                NodeSnapshot(text = "Edit profile")
            )
        )

        val result = classifier.classify(profileNode, null)

        assertEquals(ScreenType.PROFILE, result.screenType)
        assertTrue(!result.screenType.isDistractionSurface)
    }

    @Test
    fun testUnknownScreenSafeFallback() {
        val ambiguousNode = NodeSnapshot(
            text = "Settings & Privacy",
            children = listOf(
                NodeSnapshot(text = "Account Center"),
                NodeSnapshot(text = "Password and security")
            )
        )

        val result = classifier.classify(ambiguousNode, null)

        assertEquals(ScreenType.UNKNOWN, result.screenType)
        assertEquals(0.0f, result.confidence, 0.01f)
    }
}

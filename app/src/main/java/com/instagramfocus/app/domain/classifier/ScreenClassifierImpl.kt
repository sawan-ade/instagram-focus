package com.instagramfocus.app.domain.classifier

import com.instagramfocus.app.domain.model.ScreenType

/**
 * Multi-signal classifier that evaluates Instagram UI tree snapshots and activity context.
 *
 * Priorities:
 * 1. Communication features (DMs, Stories, Follow Requests) are checked with high precedence
 *    to prevent false positives.
 * 2. High confidence is required before classifying a screen as a distraction surface (Reels, Explore, Feed).
 * 3. Safe fallback: If signals are ambiguous or missing, ScreenType.UNKNOWN is returned.
 */
class ScreenClassifierImpl : ScreenClassifier {

    override fun classify(rootNode: NodeSnapshot?, activityName: String?): ClassificationResult {
        if (rootNode == null && activityName == null) {
            return ClassificationResult.unknown("Root node and activity are both null")
        }

        val simpleActivity = activityName?.substringAfterLast(".") ?: ""

        // Check explicit activity signatures first if available
        if (InstagramAdapter.Activities.STORY_ACTIVITIES.contains(simpleActivity)) {
            return ClassificationResult(
                screenType = ScreenType.STORY,
                confidence = 0.98f,
                matchedRules = listOf("ActivitySignature: $simpleActivity")
            )
        }

        if (InstagramAdapter.Activities.DM_ACTIVITIES.contains(simpleActivity)) {
            return ClassificationResult(
                screenType = ScreenType.DM_CONVERSATION,
                confidence = 0.98f,
                matchedRules = listOf("ActivitySignature: $simpleActivity")
            )
        }

        if (InstagramAdapter.Activities.REELS_ACTIVITIES.contains(simpleActivity)) {
            return ClassificationResult(
                screenType = ScreenType.REELS,
                confidence = 0.98f,
                matchedRules = listOf("ActivitySignature: $simpleActivity")
            )
        }

        if (rootNode == null) {
            return ClassificationResult.unknown("No accessibility node available")
        }

        val allTexts = rootNode.collectAllText().map { it.lowercase() }
        val allIds = rootNode.collectAllIds().map { it.lowercase() }
        val matchedSignals = mutableListOf<String>()

        // 1. Check for DM Conversation (Highest communication priority)
        val hasComposerInput = allTexts.any { it.startsWith("message") || it.contains("send message") || it.contains("message...") }
        val hasComposerId = allIds.any { id ->
            id.contains("composer") || id.contains("row_thread_composer") || id.contains("direct_voice")
        }
        if (hasComposerInput && (hasComposerId || allTexts.any { it.contains("camera") || it.contains("gallery") })) {
            matchedSignals.add("DM Composer input detected")
            return ClassificationResult(
                screenType = ScreenType.DM_CONVERSATION,
                confidence = 0.95f,
                matchedRules = listOf("Rule: DmConversationInput"),
                detectedSignals = matchedSignals
            )
        }

        // 2. Check for Stories
        val hasStoryReply = allTexts.any { it.startsWith("reply to") || it.contains("like story") || it.contains("send a message") }
        val hasStoryId = allIds.any { id ->
            id.contains("reel_viewer_progress_bar") || id.contains("toolbar_composer") || id.contains("reel_composer")
        }
        if (hasStoryReply || hasStoryId) {
            matchedSignals.add("Story viewer elements detected")
            return ClassificationResult(
                screenType = ScreenType.STORY,
                confidence = 0.95f,
                matchedRules = listOf("Rule: StoryViewerElements"),
                detectedSignals = matchedSignals
            )
        }

        // 3. Check for Follow Requests
        val hasFollowRequestText = allTexts.any { text ->
            InstagramAdapter.Keywords.FOLLOW_REQUEST_KEYWORDS.any { kw -> text.contains(kw) }
        }
        if (hasFollowRequestText) {
            matchedSignals.add("Follow request keywords detected")
            return ClassificationResult(
                screenType = ScreenType.FOLLOW_REQUESTS,
                confidence = 0.95f,
                matchedRules = listOf("Rule: FollowRequestsScreen"),
                detectedSignals = matchedSignals
            )
        }

        // 4. Check for DM Inbox
        val hasDmInboxTitle = allTexts.any { it == "messages" || it == "chats" || it == "direct" }
        val hasDmTabs = allTexts.any { it == "primary" || it == "general" || it == "requests" }
        val hasDmId = allIds.any { id ->
            id.contains("direct_inbox") || id.contains("row_inbox_container") || id.contains("message_list")
        }
        if ((hasDmInboxTitle && hasDmTabs) || (hasDmId && hasDmInboxTitle)) {
            matchedSignals.add("DM Inbox headers and tabs detected")
            return ClassificationResult(
                screenType = ScreenType.DM_INBOX,
                confidence = 0.92f,
                matchedRules = listOf("Rule: DmInboxHeaders"),
                detectedSignals = matchedSignals
            )
        }

        // 5. Check for Activity / Notifications
        val hasActivityTitle = allTexts.any { it == "activity" || it == "notifications" }
        val hasSocialInteractions = allTexts.any { text ->
            text.contains("liked your") || text.contains("commented:") || text.contains("started following you") || text.contains("mentioned you")
        }
        if (hasActivityTitle || hasSocialInteractions) {
            matchedSignals.add("Activity/Notifications social interactions detected")
            return ClassificationResult(
                screenType = ScreenType.NOTIFICATIONS,
                confidence = 0.90f,
                matchedRules = listOf("Rule: ActivityNotificationsFeed"),
                detectedSignals = matchedSignals
            )
        }

        // 6. Check for Profile
        val hasProfileHeader = allTexts.any { it == "posts" } && allTexts.any { it == "followers" } && allTexts.any { it == "following" }
        val hasProfileActions = allTexts.any { it.contains("edit profile") || it.contains("share profile") || it.contains("highlights") }
        val hasProfileId = allIds.any { id -> id.contains("profile_tab") || id.contains("row_profile_header") }
        if (hasProfileHeader || (hasProfileActions && hasProfileId)) {
            matchedSignals.add("Profile stats and layout detected")
            return ClassificationResult(
                screenType = ScreenType.PROFILE,
                confidence = 0.92f,
                matchedRules = listOf("Rule: UserProfileScreen"),
                detectedSignals = matchedSignals
            )
        }

        // 7. Check for Reels (Distraction Surface)
        val hasReelsKeyword = allTexts.any { text ->
            InstagramAdapter.Keywords.REELS_KEYWORDS.any { kw -> text.contains(kw) }
        }
        val hasReelsId = allIds.any { id ->
            InstagramAdapter.ViewIds.REELS_IDS.any { target -> id.contains(target) }
        }
        if (hasReelsKeyword || hasReelsId) {
            matchedSignals.add("Reels markers detected: keyword=$hasReelsKeyword, id=$hasReelsId")
            return ClassificationResult(
                screenType = ScreenType.REELS,
                confidence = if (hasReelsKeyword && hasReelsId) 0.96f else 0.88f,
                matchedRules = listOf("Rule: ReelsDistractionSurface"),
                detectedSignals = matchedSignals
            )
        }

        // 8. Check for Explore (Distraction Surface)
        val hasExploreKeyword = allTexts.any { text ->
            InstagramAdapter.Keywords.EXPLORE_KEYWORDS.any { kw -> text.contains(kw) }
        }
        val hasExploreId = allIds.any { id ->
            InstagramAdapter.ViewIds.EXPLORE_IDS.any { target -> id.contains(target) }
        }
        if (hasExploreKeyword || hasExploreId) {
            matchedSignals.add("Explore grid / search markers detected")
            return ClassificationResult(
                screenType = ScreenType.EXPLORE,
                confidence = if (hasExploreKeyword && hasExploreId) 0.94f else 0.86f,
                matchedRules = listOf("Rule: ExploreSearchGrid"),
                detectedSignals = matchedSignals
            )
        }

        // 9. Check for Home Feed (Distraction Surface)
        val hasFeedId = allIds.any { id ->
            InstagramAdapter.ViewIds.FEED_IDS.any { target -> id.contains(target) }
        }
        val hasSuggestedPosts = allTexts.any { text ->
            InstagramAdapter.Keywords.SUGGESTED_FEED_KEYWORDS.any { kw -> text.contains(kw) }
        }
        val hasHomeTabSelected = allTexts.any { it == "home" } && rootNode.any { it.contentDescription?.contains("Home", ignoreCase = true) == true }
        if (hasFeedId || hasSuggestedPosts || hasHomeTabSelected) {
            matchedSignals.add("Home feed markers detected: feedId=$hasFeedId, suggested=$hasSuggestedPosts, homeTab=$hasHomeTabSelected")
            return ClassificationResult(
                screenType = ScreenType.HOME_FEED,
                confidence = if (hasSuggestedPosts) 0.92f else 0.84f,
                matchedRules = listOf("Rule: HomeFeedSurface"),
                detectedSignals = matchedSignals
            )
        }

        // Safe Fallback: Unknown
        return ClassificationResult.unknown("Ambiguous or unrecognized screen pattern")
    }
}

package com.instagramfocus.app.domain.classifier

/**
 * Encapsulates known identifiers, activity names, content descriptions, and UI keywords
 * used by Instagram across various app versions.
 * Keeping these abstracted allows updating detection heuristics as Instagram evolves
 * without modifying the core classifier or restriction engine.
 */
object InstagramAdapter {

    const val PACKAGE_NAME = "com.instagram.android"
    const val DEEP_LINK_DIRECT_INBOX = "instagram://direct_inbox"
    const val DEEP_LINK_NOTIFICATIONS = "instagram://notifications"
    const val WEB_DIRECT_FALLBACK = "https://www.instagram.com/direct/inbox/"

    // Activity signatures
    object Activities {
        val DM_ACTIVITIES = setOf(
            "DirectInboxActivity",
            "DirectThreadActivity",
            "DirectMessageActivity",
            "DirectShareSheetActivity"
        )
        val STORY_ACTIVITIES = setOf(
            "ReelViewerActivity",
            "StoryViewerActivity"
        )
        val REELS_ACTIVITIES = setOf(
            "ClipsViewerActivity",
            "ReelsViewerActivity"
        )
    }

    // View resource ID fragments
    object ViewIds {
        val REELS_IDS = setOf(
            "clips_viewer",
            "clips_video_container",
            "reel_viewer_title",
            "clips_ufi_like_button",
            "clips_swipe_refresh_layout",
            "reels_tab"
        )
        val EXPLORE_IDS = setOf(
            "explore_grid",
            "action_bar_search_edit_text",
            "search_tab",
            "explore_tab"
        )
        val FEED_IDS = setOf(
            "feed_tab",
            "main_feed",
            "action_bar_title_logo",
            "row_feed_button_like",
            "row_feed_button_comment"
        )
        val DM_IDS = setOf(
            "direct_inbox",
            "row_inbox_container",
            "message_list",
            "direct_tab",
            "row_thread_composer_edittext",
            "message_composer",
            "direct_voice_button"
        )
        val STORY_IDS = setOf(
            "reel_viewer_progress_bar",
            "toolbar_composer",
            "reel_composer"
        )
        val PROFILE_IDS = setOf(
            "profile_tab",
            "row_profile_header",
            "profile_tab_layout"
        )
    }

    // Content descriptions and visible texts
    object Keywords {
        val REELS_KEYWORDS = listOf(
            "reels",
            "reel by",
            "remix this reel",
            "audio used in this reel",
            "watch more reels",
            "use audio",
            "original audio"
        )
        val EXPLORE_KEYWORDS = listOf(
            "search and explore",
            "explore grid",
            "search instagram",
            "discover accounts",
            "suggested searches"
        )
        val DM_KEYWORDS = listOf(
            "messages",
            "chats",
            "direct",
            "message requests",
            "primary",
            "general",
            "message...",
            "send message",
            "direct camera"
        )
        val STORY_KEYWORDS = listOf(
            "reply to",
            "send message to",
            "like story",
            "story by",
            "your story"
        )
        val FOLLOW_REQUEST_KEYWORDS = listOf(
            "follow requests",
            "requests to follow",
            "confirm request",
            "delete request",
            "requested to follow you"
        )
        val NOTIFICATION_KEYWORDS = listOf(
            "notifications",
            "activity",
            "liked your story",
            "commented on your",
            "started following you",
            "mentioned you in"
        )
        val PROFILE_KEYWORDS = listOf(
            "edit profile",
            "share profile",
            "posts",
            "followers",
            "following",
            "highlights"
        )
        val SUGGESTED_FEED_KEYWORDS = listOf(
            "suggested for you",
            "suggested posts",
            "you're all caught up",
            "older posts"
        )
    }
}

package com.instagramfocus.app.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramfocus.app.data.repository.FocusSettingsRepository
import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.classifier.NodeSnapshot
import com.instagramfocus.app.domain.classifier.ScreenClassifier
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.domain.restriction.RestrictionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DebugUiState(
    val currentPackage: String = "com.instagram.android",
    val simulatedScreen: String = "REELS",
    val classification: ClassificationResult? = null,
    val decision: RestrictionDecision? = null
)

class DebugViewModel(
    private val classifier: ScreenClassifier,
    private val restrictionEngine: RestrictionEngine,
    private val settingsRepository: FocusSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    init {
        simulateScreen("REELS")
    }

    fun simulateScreen(type: String) {
        viewModelScope.launch {
            val settings: FocusSettings = settingsRepository.settingsFlow.first()
            val bypass: BypassState = settingsRepository.bypassFlow.first()

            val snapshot = when (type) {
                "REELS" -> NodeSnapshot(
                    text = "Reels",
                    contentDescription = "Reel by creator",
                    viewIdResourceName = "com.instagram.android:id/clips_viewer_view_pager",
                    children = listOf(
                        NodeSnapshot(text = "Original audio", contentDescription = "Audio used in this reel"),
                        NodeSnapshot(contentDescription = "Like reel")
                    )
                )
                "FEED" -> NodeSnapshot(
                    text = "Home",
                    viewIdResourceName = "com.instagram.android:id/feed_tab",
                    children = listOf(
                        NodeSnapshot(text = "Suggested for you"),
                        NodeSnapshot(text = "You're all caught up")
                    )
                )
                "EXPLORE" -> NodeSnapshot(
                    contentDescription = "Search and explore",
                    viewIdResourceName = "com.instagram.android:id/explore_grid",
                    children = listOf(
                        NodeSnapshot(text = "Search instagram")
                    )
                )
                "DM_INBOX" -> NodeSnapshot(
                    text = "Messages",
                    viewIdResourceName = "com.instagram.android:id/direct_inbox",
                    children = listOf(
                        NodeSnapshot(text = "Primary"),
                        NodeSnapshot(text = "General")
                    )
                )
                "DM_CHAT" -> NodeSnapshot(
                    text = "Message...",
                    viewIdResourceName = "com.instagram.android:id/row_thread_composer_edittext",
                    children = listOf(
                        NodeSnapshot(contentDescription = "Send message"),
                        NodeSnapshot(contentDescription = "Direct camera")
                    )
                )
                "STORY" -> NodeSnapshot(
                    text = "Reply to user...",
                    viewIdResourceName = "com.instagram.android:id/reel_viewer_progress_bar",
                    children = listOf(
                        NodeSnapshot(contentDescription = "Like story")
                    )
                )
                "PROFILE" -> NodeSnapshot(
                    viewIdResourceName = "com.instagram.android:id/profile_tab",
                    children = listOf(
                        NodeSnapshot(text = "Posts"),
                        NodeSnapshot(text = "Followers"),
                        NodeSnapshot(text = "Following"),
                        NodeSnapshot(text = "Edit profile")
                    )
                )
                "FOLLOW_REQUESTS" -> NodeSnapshot(
                    text = "Follow requests",
                    children = listOf(
                        NodeSnapshot(text = "Confirm"),
                        NodeSnapshot(text = "Delete")
                    )
                )
                else -> NodeSnapshot(text = "Unknown screen", children = emptyList())
            }

            val classification = classifier.classify(snapshot, null)
            val decision = restrictionEngine.evaluate(classification, settings, bypass)

            _uiState.value = _uiState.value.copy(
                simulatedScreen = type,
                classification = classification,
                decision = decision
            )
        }
    }
}

# Instagram Focus — Technical Architecture & Design

This document details the internal design, classification heuristics, and Android system mechanics used by Instagram Focus.

---

## 1. System Overview & Constraints

Instagram Focus is an intentional companion layer for the official Android Instagram client (`com.instagram.android`). It is designed around three strict operational constraints:

1. **Zero Credential / API Interception**: No reverse-engineered private endpoints, no OAuth hijack, no session cookies, and no network proxying (no MITM CA certificates).
2. **Minimal Android Privilege Profile**: The app runs without `SYSTEM_ALERT_WINDOW` ("Draw over other apps") and without storage/phone permissions. Interventions are presented through standard Activity intent dispatching (`FLAG_ACTIVITY_NEW_TASK`), and screen state is observed solely through an accessibility bridge scoped exclusively to `com.instagram.android`.
3. **Battery & CPU Efficiency**: The service remains completely passive until Instagram enters the foreground. Accessibility window events are debounced (120ms) and tree inspection is strictly bounded to a maximum depth of 6 levels.

---

## 2. Component Pipeline

```
  ┌────────────────────────────────────────────────────────┐
  │         Official Instagram Client Window               │
  │            (com.instagram.android)                     │
  └──────────────────────────┬─────────────────────────────┘
                             │ AccessibilityEvent
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │              FocusAccessibilityService                 │
  │   - Filters package to com.instagram.android           │
  │   - 120ms Debouncer prevents redundant scans           │
  │   - Extracts lightweight NodeSnapshot (depth <= 6)     │
  └──────────────────────────┬─────────────────────────────┘
                             │ NodeSnapshot
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │                 ScreenClassifier                       │
  │   - Rule heuristics (IDs, content descriptions, texts) │
  │   - ClassificationResult(type, confidence, rules)     │
  └──────────────────────────┬─────────────────────────────┘
                             │ ClassificationResult
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │                RestrictionEngine                       │
  │   - Evaluates against FocusSettings & BypassState      │
  │   - Decision: ALLOW | BLOCK | WARN                     │
  │   - Safe fallback: UNKNOWN screens default to ALLOW    │
  └──────────────────────────┬─────────────────────────────┘
                             │ If BLOCK
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │               InterventionActivity                     │
  │   - Launches calm translucent Activity                 │
  │   - Records block event to local SQLite database       │
  │   - Quick-launch intents: DMs / Notifications          │
  └────────────────────────────────────────────────────────┘
```

---

## 3. Screen Classifier Heuristics

Instagram updates its internal layout and view IDs periodically. To ensure resilience, `ScreenClassifierImpl` does not rely on single hard-coded string comparisons. Instead, it aggregates structural evidence:

### A. Reels Detection (`ScreenType.REELS`)
* **Primary Resource IDs**: `com.instagram.android:id/clips_viewer_view_pager`, `clips_video_container`, `reel_viewer_aspect_ratio_layout`.
* **Content Descriptions**: `"Reel by"`, `"Audio by"`, `"Remix with"`.
* **Text Signatures**: `"Original audio"`, `"Use template"`, `"Watch full reel"`.
* **Structural Shape**: Full-screen vertical swipe containers with embedded audio attribution pills.

### B. Explore Grid (`ScreenType.EXPLORE`)
* **Primary Resource IDs**: `com.instagram.android:id/explore_grid`, `search_tab_layout`, `search_and_explore`.
* **Content Descriptions**: `"Search and explore"`, `"Discover people"`.
* **Text Signatures**: `"Search"`, `"Trending"`, `"Suggested reels"`.

### C. Home Feed (`ScreenType.HOME_FEED`)
* **Markers**: Main bottom bar `feed_tab` selected.
* **Algorithmic Section Detection**: Nodes matching `"Suggested for you"`, `"Suggested posts"`, or `"Because you watched"`.
* **Caught-Up Boundary**: Scrolling events occurring below the `"You're all caught up"` milestone marker.

### D. Direct Messages (`ScreenType.DM_INBOX` & `ScreenType.DM_CONVERSATION`) — High Priority Allow
* **Inbox Markers**: `direct_inbox`, `action_bar_inbox`, tabs for `"Primary"` / `"General"`.
* **Conversation Markers**: `row_thread_composer_edittext`, `message_send_button`, `voice_record_button`, `"Message..."` placeholder.
* **Safety Rule**: If any message composer node or thread header is detected, confidence immediately locks to `0.98+` for `DM_CONVERSATION` to guarantee communication is never interrupted.

### E. Stories (`ScreenType.STORY`) — Allowed
* **Markers**: `reel_viewer_progress_bar`, `story_reply_composer`, `"Reply to [user]..."`.
* **Behavior**: Full viewing allowed. Optional caps can be configured in settings to prevent infinite story daisy-chaining.

### F. Safe Fallback Principle
If confidence is `< 0.60` or the layout does not decisively match a known distraction pattern, the engine classifies the screen as `ScreenType.UNKNOWN`.

`RestrictionEngineImpl` treats `UNKNOWN` as **ALLOW**:
```kotlin
ScreenType.UNKNOWN -> RestrictionDecision.Allow("Safe fallback: uncertain screen layout")
```
This guarantees that app updates or unmapped screens will never mistakenly lock the user out of useful communication.

---

## 4. Local Storage & Privacy Schema

* **Settings & Timers**: Managed via `FocusPreferencesImpl` on Android `SharedPreferences` exposed as reactive Kotlin `StateFlow` primitives.
* **Protection Metrics**: Persisted in an on-device SQLite database (`FocusDatabaseHelper`).
  * `block_events`: Records `timestamp`, `screen_type`, and `reason` (auto-pruned after 30 days).
  * `daily_aggregates`: Stores daily count summaries for Reels, Explore, Feed, and DM sessions.
  * **No Network Permissions**: The application does not declare `android.permission.INTERNET`. Zero bytes leave the handset.

---

## 5. Android 14/15/16 Compatibility & Keystore

* **Target SDK**: Configured for `minSdkVersion 26` (Android 8.0 Oreo) and `targetSdkVersion 34` (Android 14 UpsideDownCake, fully forward-compatible with Android 15 & 16).
* **Package Signatures**: Signed with modern APK Signature Scheme **v2** and **v3** blocks to pass Play Protect and OEM package installer verifications without triggering deprecated SDK blocks (`INSTALL_FAILED_DEPRECATED_SDK_VERSION`).

---

## 6. Inherent Architectural Limitations & Non-Viability of Custom Clients

### The Sandboxing & Process Isolation Trade-off
Under Android's Linux security model, `com.instagram.android` and `com.instagramfocus.app` run under distinct, isolated user IDs (UIDs). 

1. **Reactive vs. Preemptive Intervention**:
   Because Instagram Focus is not injected into Instagram's ART runtime (which would require root access via LSPosed/Magisk and invalidate SafetyNet/Play Integrity), our app operates **reactively**. When the user taps a Reels tab:
   * Instagram begins drawing its initial frame buffer.
   * The Android `WindowManager` posts an `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED` or `TYPE_WINDOW_CONTENT_CHANGED`.
   * Our 120ms debouncer processes the event, parses the accessibility tree, and triggers `InterventionActivity`.
   * This leaves an unavoidable, non-zero window (~100–150ms) where the user might see a brief fraction-of-a-second flash of the video before the block screen appears.

2. **Why a "Clean Custom Instagram Remake" Cannot Be Built Safely**:
   * **No Public Personal API**: Meta only exposes the Instagram Graph API for business marketing accounts. There is no official API for personal direct messages, user stories, or following feed endpoints.
   * **Aggressive Device Fingerprinting**: Meta uses custom encrypted binary protocols over MQTT/GraphQL with TLS fingerprinting (JA3/JA4), automated challenge checks (SMS/selfie verification), and account risk scoring. Unofficial clients (e.g. reverse-engineered clones or modded APKs like Instander) frequently trigger automated security checkpoints and account bans.
   * **Conclusion**: Working as an external observer around the legitimate Google Play Store client is the **only architecture that guarantees 100% account safety and zero risk of suspension**, despite the inherent constraint of being an external layer.

3. **Fail-Open Safe Fallback Strategy**:
   If an Instagram A/B test modifies component view IDs or structure such that `ScreenClassifier` confidence drops below `0.60`, the system classifies the screen as `UNKNOWN` and defaults to `ALLOW`. This intentional fail-open design guarantees that legitimate emergency communications or time-sensitive direct messages are never mistakenly obstructed by false positives.


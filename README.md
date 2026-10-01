# Instagram Focus

[![Android](https://img.shields.io/badge/Android-8.0%20to%2016-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device-10B981)](#privacy-guarantee)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> *"Instagram as a communication tool, not an entertainment app."*

**Instagram Focus** is a private, lightweight Android utility built to filter out the addictive surfaces of Instagram while preserving the useful social features.

It runs locally on your phone alongside the official Instagram app (`com.instagram.android`). It doesn't replace the app, hijack your session, or touch your account credentials. Instead, it uses Android's Accessibility framework to identify when an infinite-scrolling feed is active and steps in with a calm, non-punitive intervention screen.

<p align="center">
  <img src="docs/screenshots/dashboard.jpg" width="260" alt="Instagram Focus Dashboard Hub" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/screenshots/intervention.jpg" width="260" alt="Calm Intervention Screen" />
</p>

---

## Allowed vs. Blocked Surfaces

The app actively differentiates intentional social communication from algorithmic consumption loops:

<p align="center">
  <img src="docs/screenshots/onboarding-allowed.jpg" width="260" alt="Allowed Features" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/screenshots/onboarding-blocked.jpg" width="260" alt="Blocked Distractions" />
</p>

| ✅ Allowed (Communication) | ❌ Blocked (Algorithmic Traps) |
| :--- | :--- |
| **Direct Messages (DMs)** (Chats, Photos, Voice notes) | **Instagram Reels** (Endless vertical video feed) |
| **Instagram Stories** (Updates from people you follow) | **Explore Grid** (Algorithmic discovery rabbit holes) |
| **Follow Requests** (Approve or decline requests) | **Endless Feed Scrolling** (Posts below "You're all caught up") |
| **User Profiles** (Looking up specific accounts) | **Suggested Content** (Un-followed accounts & recommendations) |
| **Social Notifications** (DMs, mentions, follow requests) | **Promotional Alerts** (Reels push notifications) |

---

## How It Works Under the Hood

Instagram Focus is built around four core design principles:

1. **Intelligent Screen Classifier (`ScreenClassifier`)**: Rather than relying on fragile hard-coded text matches, the classifier evaluates multiple structural signals: view IDs (`clips_viewer_view_pager`, `explore_grid`, `feed_tab`), accessibility hierarchy nodes, and content descriptions.
2. **Safe Fallback Policy**: If a screen layout is ambiguous (e.g. after an Instagram UI update), the engine defaults to **ALLOW** (`ScreenType.UNKNOWN -> ALLOW`). Legitimate communication is never accidentally locked.
3. **Zero Invasive Permissions**: The app does **not** request `SYSTEM_ALERT_WINDOW` ("Draw over other apps"). The calm intervention screen (`InterventionActivity`) runs as a standard high-priority Android Activity with a translucent theme (`FLAG_ACTIVITY_NEW_TASK`).
4. **Battery-Conscious Performance**:
   - The Accessibility Service binds strictly to package `com.instagram.android`.
   - Node parsing is debounced at 120ms to prevent duplicate processing during fast flings.
   - Hierarchy traversal stops at depth 6.
   - Zero background polling timers. When Instagram is closed, CPU usage is zero.

---

## Getting Started

### 1. Installation

Download the pre-compiled, signed APK from the [Releases](https://github.com/sawan-ade/instagram-focus/releases) section:

- **[app-release.apk](app-release.apk)** (v1.0.0, 1.2 MB)

Transfer it to your phone via USB, WhatsApp, or Google Drive, and tap to install.

### 2. Permissions & Unlocking "Restricted Settings" (Android 13, 14, 15 & 16)

<p align="center">
  <img src="docs/screenshots/onboarding-permissions.jpg" width="260" alt="Permissions Setup Screen" />
</p>

On modern Android versions (including Realme UI, Pixel, Samsung One UI, and ColorOS), Android automatically greys out the Accessibility toggle for sideloaded apps as a security precaution. To unlock it:

1. Long-press the **Instagram Focus** icon on your home screen or app drawer and tap **App Info** (ⓘ).
2. Tap the **three vertical dots (⋮)** in the top-right corner.
3. Tap **"Allow restricted settings"** and confirm with your fingerprint or device PIN.
4. Go to **Settings $\rightarrow$ Accessibility $\rightarrow$ Downloaded apps $\rightarrow$ Instagram Focus** and toggle it **ON**.

---

## Core Controls & Features

* **Master Switch**: Toggle Focus Mode ON/OFF at any time. Turning it off displays an optional reflection prompt (*"Why are you pausing focus?"*) with an option to take a 10-minute break instead.
* **Temporary Bypass**: Quickly pause restrictions for `5 min`, `10 min`, or `30 min` when you need to check something specific. A live countdown shows when protection resumes.
* **Direct DM Launcher**: Tap `Open Direct Messages Directly` to deep-link directly into your inbox (`instagram://direct_inbox`), bypassing the feed entirely.
* **Distraction Protection Stats**: Track how many times you were saved from falling into Reels or Explore today.
* **Live Classifier Inspector**: A built-in diagnostic tool to test and simulate how the classifier categorizes different Instagram screen states in real time.

---

## Privacy Guarantee

This project was built for personal use with privacy as the primary architectural constraint:

- **No Internet Permission**: The app doesn't even declare `android.permission.INTERNET`. Not a single packet can leave your device.
- **No Account Access**: Never asks for your Instagram password, tokens, or cookies.
- **No Message Logging**: Accessibility node scanning only inspects layout IDs and container types. Private message contents are never read, copied, or stored.
- **Local SQLite Storage**: Aggregate distraction counters stay in an encrypted on-device database and can be cleared with one tap in Settings.

---

## Building from Source

### Prerequisites
- JDK 17
- Android SDK Platform 34 & Build-Tools 34.0.0+
- Kotlin 1.9.23+

### Build via Gradle
```bash
git clone https://github.com/sawan-ade/instagram-focus.git
cd instagram-focus
./gradlew assembleRelease
```

### Running Unit Tests
```bash
./gradlew test
```
The test suite includes 23 unit tests covering screen classification heuristics, restriction rule evaluation, and bypass state timers:
```
OK (23 tests) - 0.089s
```

---

## License

MIT License — Built by [Sawan Ade](https://github.com/sawan-ade). Free for personal use and modification.

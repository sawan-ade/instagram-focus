package com.instagramfocus.app.domain.classifier

/**
 * Classifies an on-screen state inside Instagram into a known ScreenType.
 */
interface ScreenClassifier {
    fun classify(rootNode: NodeSnapshot?, activityName: String? = null): ClassificationResult
}

package com.instagramfocus.app.domain.restriction

import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision

/**
 * Decides whether to allow, block, or warn for a given screen classification
 * based on the user's active focus configuration and temporary bypass status.
 */
interface RestrictionEngine {
    fun evaluate(
        classification: ClassificationResult,
        settings: FocusSettings,
        bypassState: BypassState
    ): RestrictionDecision
}

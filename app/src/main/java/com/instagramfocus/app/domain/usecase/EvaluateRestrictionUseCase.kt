package com.instagramfocus.app.domain.usecase

import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.domain.restriction.RestrictionEngine

class EvaluateRestrictionUseCase(
    private val engine: RestrictionEngine
) {
    operator fun invoke(
        classification: ClassificationResult,
        settings: FocusSettings,
        bypassState: BypassState
    ): RestrictionDecision {
        return engine.evaluate(classification, settings, bypassState)
    }
}

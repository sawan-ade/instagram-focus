package com.instagramfocus.app.domain.usecase

import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.classifier.NodeSnapshot
import com.instagramfocus.app.domain.classifier.ScreenClassifier

class ClassifyScreenUseCase(
    private val classifier: ScreenClassifier
) {
    operator fun invoke(rootNode: NodeSnapshot?, activityName: String?): ClassificationResult {
        return classifier.classify(rootNode, activityName)
    }
}

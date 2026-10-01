package com.instagramfocus.app

import android.content.Context
import com.instagramfocus.app.data.local.FocusDatabaseHelper
import com.instagramfocus.app.data.preferences.FocusPreferences
import com.instagramfocus.app.data.preferences.FocusPreferencesImpl
import com.instagramfocus.app.data.repository.FocusSettingsRepository
import com.instagramfocus.app.data.repository.FocusSettingsRepositoryImpl
import com.instagramfocus.app.data.repository.StatisticsRepository
import com.instagramfocus.app.data.repository.StatisticsRepositoryImpl
import com.instagramfocus.app.domain.classifier.ScreenClassifier
import com.instagramfocus.app.domain.classifier.ScreenClassifierImpl
import com.instagramfocus.app.domain.restriction.RestrictionEngine
import com.instagramfocus.app.domain.restriction.RestrictionEngineImpl
import com.instagramfocus.app.domain.usecase.ClassifyScreenUseCase
import com.instagramfocus.app.domain.usecase.EvaluateRestrictionUseCase
import com.instagramfocus.app.domain.usecase.ManageBypassUseCase
import com.instagramfocus.app.domain.usecase.RecordBlockEventUseCase

class FocusContainer(val context: Context) {

    val databaseHelper: FocusDatabaseHelper by lazy {
        FocusDatabaseHelper(context.applicationContext)
    }

    val preferences: FocusPreferences by lazy {
        FocusPreferencesImpl(context.applicationContext)
    }

    val settingsRepository: FocusSettingsRepository by lazy {
        FocusSettingsRepositoryImpl(preferences)
    }

    val statisticsRepository: StatisticsRepository by lazy {
        StatisticsRepositoryImpl(databaseHelper)
    }

    val classifier: ScreenClassifier by lazy {
        ScreenClassifierImpl()
    }

    val restrictionEngine: RestrictionEngine by lazy {
        RestrictionEngineImpl()
    }

    val classifyScreenUseCase: ClassifyScreenUseCase by lazy {
        ClassifyScreenUseCase(classifier)
    }

    val evaluateRestrictionUseCase: EvaluateRestrictionUseCase by lazy {
        EvaluateRestrictionUseCase(restrictionEngine)
    }

    val manageBypassUseCase: ManageBypassUseCase by lazy {
        ManageBypassUseCase(settingsRepository)
    }

    val recordBlockEventUseCase: RecordBlockEventUseCase by lazy {
        RecordBlockEventUseCase(statisticsRepository)
    }
}

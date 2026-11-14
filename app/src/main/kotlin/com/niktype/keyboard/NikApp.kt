package com.niktype.keyboard

import android.app.Application
import com.niktype.keyboard.data.CalibrationFileStore
import com.niktype.keyboard.data.FileLearningStore
import com.niktype.keyboard.data.SettingsRepository
import com.niktype.keyboard.data.ShortcutsStore
import com.niktype.prediction.PredictionEngine

/**
 * ظرف وابستگی‌ها (DI سبک بدون کتابخانه).
 */
class AppContainer(context: Application) {
    val appContext: android.content.Context = context.applicationContext
    val settingsRepo = SettingsRepository(appContext)
    val learningStore = FileLearningStore(appContext)
    val calibrationStore = CalibrationFileStore(appContext)
    val shortcutsStore = ShortcutsStore(appContext)
    val prediction: PredictionEngine = PredictionEngine(learningStore)
}

class NikApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

package com.strizhonovapps.lexixapp

import android.app.Application
import android.content.res.AssetManager
import com.orm.SugarContext
import com.strizhonovapps.lexixapp.notification.NotificationAlarmConfigurer
import com.strizhonovapps.lexixapp.service.WordSuggestionService
import com.strizhonovapps.lexixapp.util.getLinesFromInputStream
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApp : Application() {

    @Inject
    lateinit var wordSuggestionService: WordSuggestionService

    @Inject
    lateinit var alarmConfigurator: NotificationAlarmConfigurer

    @Inject
    lateinit var assetManager: AssetManager

    override fun onCreate() {
        SugarContext.init(this)
        super.onCreate()
        inflateSuggestedWords()
    }

    override fun onTerminate() {
        super.onTerminate()
        SugarContext.terminate()
        alarmConfigurator.setNotificationAlarm(applicationContext)
    }

    private fun inflateSuggestedWords() {
        if (wordSuggestionService.wordsForSuggestionPresent()) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = assetManager.open("word-suggestions-en.txt")
            val words = getLinesFromInputStream(inputStream)
            wordSuggestionService.saveAllWordsForSuggestion(words)
        }
    }

}
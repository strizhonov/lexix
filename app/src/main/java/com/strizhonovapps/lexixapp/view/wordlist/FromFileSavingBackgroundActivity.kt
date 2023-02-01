package com.strizhonovapps.lexixapp.view.wordlist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.strizhonovapps.lexixapp.service.LanguageService
import com.strizhonovapps.lexixapp.service.WordMetadataFacadeService
import com.strizhonovapps.lexixapp.service.WordService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FromFileSavingBackgroundActivity : ComponentActivity() {

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var languageService: LanguageService

    @Inject
    lateinit var wordMetadataFacadeService: WordMetadataFacadeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        val separator = intent.getStringExtra(SEPARATOR_EXTRA_KEY)

        if (data == null || separator == null) {
            Log.w(this::class.java.name, "Trying create activity without necessary data.")
            finish()
            return
        }
        contentResolver.openInputStream(data).use { inputStream ->
            if (inputStream == null) return@use

            val saved = wordService.saveAllFromStream(
                inputStream,
                separator,
                languageService.getStudyLanguage()
            )
            saved.forEach { word ->
                val name = word.name ?: return@forEach
                CoroutineScope(Dispatchers.Default).launch {
                    wordMetadataFacadeService.saveMetadata(word.id, name)
                }
            }
        }

        finish()
    }

}
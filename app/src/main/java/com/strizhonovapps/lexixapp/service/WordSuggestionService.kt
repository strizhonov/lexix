package com.strizhonovapps.lexixapp.service

import com.strizhonovapps.lexixapp.dao.WordSuggestionDao
import com.strizhonovapps.lexixapp.model.WordSuggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

const val MAX_ATTEMPTS = 10

@Singleton
class WordSuggestionService @Inject constructor(
    private val wordSuggestionDao: WordSuggestionDao,
    private val languageService: LanguageService,
    private val wordService: WordService,
) {

    suspend fun findNextWordForSuggestion(
        attempt: Int = 0,
        allNames: List<String>? = null
    ): Pair<String, String>? {
        if (attempt >= MAX_ATTEMPTS) return null

        val allFoundNames = allNames ?: wordService.findAllNames()
        val nextSuggestion = getNextSuggestionEntity() ?: return null

        increaseShownValue(nextSuggestion)

        val suggestionName = nextSuggestion.name
        val studyLangName = languageService.translateFromEnToStudyLang(suggestionName)
            ?: return findNextWordForSuggestion(attempt.inc(), allFoundNames)

        if (allFoundNames.contains(studyLangName))
            return findNextWordForSuggestion(attempt, allFoundNames)

        val translations = languageService.getAllTranslations(studyLangName)
        if (translations.isEmpty()) return findNextWordForSuggestion(attempt.inc(), allFoundNames)

        return Pair(studyLangName, translations.first())
    }

    fun wordsForSuggestionPresent() = !wordSuggestionDao.isEmpty()

    fun saveAllWordsForSuggestion(wordsForSuggestion: List<String>) {
        wordsForSuggestion
            .map { suggestionName -> WordSuggestion(name = suggestionName) }
            .let(wordSuggestionDao::saveAll)
    }

    private fun getNextSuggestionEntity(): WordSuggestion? =
        wordSuggestionDao.findRandomWithMinTimesShown()

    private fun increaseShownValue(suggestion: WordSuggestion) {
        CoroutineScope(Dispatchers.IO).launch {
            wordSuggestionDao.setTimesShown(suggestion.id, suggestion.timesShown.inc())
        }
    }

}
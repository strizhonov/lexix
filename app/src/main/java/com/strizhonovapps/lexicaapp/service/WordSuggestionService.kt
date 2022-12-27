package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.dao.WordSuggestionDao
import com.strizhonovapps.lexicaapp.model.WordSuggestion
import com.strizhonovapps.lexicaapp.model.WordSuggestionStatus
import javax.inject.Inject
import javax.inject.Singleton

const val MAX_ATTEMPTS = 10

@Singleton
class WordSuggestionService @Inject constructor(
    private val wordSuggestionDao: WordSuggestionDao,
    private val wordMetaDataService: WordOperationsServiceImpl,
    private val wordService: WordService
) {

    fun findNextWordForSuggestion(attempt: Int = 0): Pair<String, String>? {
        if (attempt >= MAX_ATTEMPTS) return null
        val nextSuggestion = getNextSuggestionEntity() ?: return null
        increaseShownValue(nextSuggestion)
        val suggestionName =
            nextSuggestion.name ?: throw IllegalStateException("WordSuggestion.name can't be null")
        val studyLangName = wordMetaDataService.translateFromEnToStudyLang(suggestionName)
            ?: return findNextWordForSuggestion(attempt.inc())
        if (wordService.findAll().map { it.name }
                .contains(studyLangName)) return findNextWordForSuggestion(attempt)
        val translations = wordMetaDataService.getAllTranslations(studyLangName)
        if (translations.isEmpty()) return findNextWordForSuggestion(attempt.inc())
        return Pair(studyLangName, translations.first())
    }

    fun isNoWordsForSuggestion() = wordSuggestionDao.isEmpty()

    fun saveAllWordsForSuggestion(wordsForSuggestion: List<String>) {
        wordsForSuggestion
            .map { WordSuggestion(name = it) }
            .let(wordSuggestionDao::saveAll)
    }

    private fun getNextSuggestionEntity() = wordSuggestionDao.findAll()
        .filter {
            val studyLanguage = wordMetaDataService.getStudyLanguage()
            it.wordSuggestionStatus == null
                    || it.wordSuggestionStatus?.langCode == studyLanguage.toString()
        }
        .shuffled()
        .minWithOrNull(Comparator.comparingInt {
            if (it.wordSuggestionStatus == null) 0
            else it.wordSuggestionStatus?.timesShown ?: 0
        })

    private fun increaseShownValue(suggestion: WordSuggestion) {
        val wordSuggestionStatus = suggestion.wordSuggestionStatus
        if (wordSuggestionStatus == null) {
            suggestion.wordSuggestionStatus = WordSuggestionStatus(
                langCode = wordMetaDataService.getStudyLanguage().toString(),
                timesShown = 1
            )
        } else {
            wordSuggestionStatus.timesShown = wordSuggestionStatus.timesShown.inc()
        }
    }

}
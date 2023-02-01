package com.strizhonovapps.lexixapp.service

import com.strizhonovapps.lexixapp.dao.WordDaoFilter
import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.model.TrainingType
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.view.wordlist.WordsBatch
import java.io.InputStream

interface WordService {

    fun add(word: Word): Long
    fun update(word: Word): Word
    fun delete(id: Long)
    fun erase(): Int
    fun count(filter: WordDaoFilter? = null): Long

    fun saveAllFromStream(
        inputStream: InputStream,
        separator: String,
        studyLanguage: SupportedLanguage
    ): List<Word>

    fun saveAudioUrlAndTranscription(wordId: Long, audioUrl: String?, transcription: String?)
    fun processKnown(word: Word)
    fun skipWord(word: Word)
    fun processUnknown(word: Word)
    fun archive(id: Long)
    fun unarchive(id: Long)
    fun uncheckHard(id: Long)

    fun resetClosestWordsDates(count: Int)
    fun findAllNames(): List<String>
    fun resetProgress()
    fun get(id: Long): Word?
    fun findAll(
        filter: WordDaoFilter? = null,
        batch: WordsBatch? = null,
        orderByIdDesc: Boolean? = null
    ): List<Word>

    fun findDuplicatesByName(): List<Word>
    fun findDuplicatesByTranslation(): List<Word>
    fun getCountOfReadyForTrainingWords(): Int

    fun getCurrentWordAndCountOfActiveWords(
        trainingType: TrainingType?,
        wordToExclude: Word?
    ): Pair<Word?, Int>

    fun getAverageExpectedWordsAndCountOfActiveWords(): Pair<Int, Int>
}

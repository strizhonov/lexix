package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.model.Word
import java.io.InputStream

interface WordService {

    fun add(word: Word): Long
    fun update(word: Word): Long
    fun delete(id: Long)
    fun erase(): Int
    fun saveAllFromStream(inputStream: InputStream, separator: String)
    fun saveAudioUrlAndTranscription(wordId: Long, audioUrl: String?, transcription: String?)
    fun processKnown(current: Word, activeWords: Int)
    fun processUnknown(current: Word, activeWords: Int)
    fun archive(id: Long)
    fun unarchive(id: Long)
    fun resetTimesShown(id: Long)
    fun resetClosestWordsDates(count: Int)
    fun resetProgress()

    fun get(id: Long): Word?
    fun count(): Long
    fun findAll(): List<Word>
    fun findAllHard(): List<Word>
    fun findAllArchived(): List<Word>
    fun findAllAvailableForStats(): List<Word>
    fun findAllAvailableForTraining(): List<Word>
    fun getCountOfReadyForTrainingWords(): Int
    fun getCurrentWordAndCountOfActiveWords(): Pair<Word?, Int>
}

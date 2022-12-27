package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.dao.WordDao
import com.strizhonovapps.lexicaapp.model.INITIAL_WORD_LEVEL
import com.strizhonovapps.lexicaapp.model.Word
import com.strizhonovapps.lexicaapp.servicehelper.WordFreezeTimeDefiner
import com.strizhonovapps.lexicaapp.servicehelper.WordsParser
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordServiceImpl @Inject constructor(
    private val wordDao: WordDao,
    private val wordsParser: WordsParser,
    private val freezeTimeDefiner: WordFreezeTimeDefiner,
) : WordService {

    override fun add(word: Word): Long {
        word.targetDate = Date(System.currentTimeMillis() + (TimeUnit.HOURS.toMillis(12)));
        return wordDao.save(word)
    }

    override fun get(id: Long) = wordDao.get(id)
    override fun update(word: Word) = wordDao.merge(word)
    override fun delete(id: Long) = wordDao.delete(id)
    override fun findAll() = wordDao.findAll()
    override fun erase() = wordDao.erase()
    override fun count() = wordDao.size()

    override fun findAllAvailableForStats(): List<Word> = findAllNonArchived()

    override fun findAllAvailableForTraining(): List<Word> =
        findAllNonArchived().filter { word -> word.isHard().not() }

    override fun findAllHard(): List<Word> = wordDao.findAll().filter(Word::isHard)

    override fun findAllArchived(): List<Word> = wordDao.findAll().filter(Word::isArchived)

    override fun getCountOfReadyForTrainingWords(): Int = getReadyForTrainingWords().size

    override fun getCurrentWordAndCountOfActiveWords(): Pair<Word?, Int> {
        val activeWords = getReadyForTrainingWords()
        val closestWord = activeWords.minByOrNull(Word::targetDate)
        return Pair(closestWord, activeWords.size)
    }

    override fun processKnown(current: Word, activeWords: Int) {
        current.timesShown = current.timesShown.inc()
        current.level = current.level.inc()
        current.modificationDate = Date()
        val freezeTimeMs = freezeTimeDefiner.define(current.level, activeWords)
        current.targetDate = Date(System.currentTimeMillis() + freezeTimeMs)
        wordDao.merge(current)
    }

    /**
     * Process unknown to user word by decreasing its level and setting new dates.
     * If current level is 1, decreasing is skipped
     *
     * @param current word to process
     */
    override fun processUnknown(current: Word, activeWords: Int) {
        current.timesShown = current.timesShown.inc()
        current.level = 1.coerceAtLeast(current.level - 1)
        current.modificationDate = Date()
        val freezeTimeMs = freezeTimeDefiner.define(current.level, activeWords)
        current.targetDate = Date(System.currentTimeMillis() + freezeTimeMs)
        wordDao.merge(current)
    }

    /**
     * Reset closest words' target date so that they are to become available for
     * learning mode
     *
     * @param count amount of words to reset
     */
    override fun resetClosestWordsDates(count: Int) =
        wordDao.findAll()
            .filter { word -> word.isArchived.not() }
            .filter { word -> word.isHard().not() }
            .sortedBy(Word::targetDate)
            .take(count)
            .forEach { word ->
                word.targetDate = Date()
                word.modificationDate = Date()
                wordDao.merge(word)
            }

    /**
     * Transfer word and its translation from Input Stream.
     * Need to have SEPARATOR between the word and its translation.
     * Every word + translation need to be on the own line, otherwise
     * method execution correctness not guaranteed.
     */
    override fun saveAllFromStream(inputStream: InputStream, separator: String) =
        wordsParser.parse(inputStream, separator).forEach(wordDao::merge)

    /**
     * Sets every word's level to INITIAL
     * and resets timesShown to 0
     */
    override fun resetProgress() =
        findAllNonArchived().forEach { word ->
            word.level = INITIAL_WORD_LEVEL
            word.timesShown = 0
            word.targetDate = Date()
            word.modificationDate = Date()
            wordDao.merge(word)
        }

    override fun archive(id: Long) {
        this.get(id)
            ?.let { word ->
                word.isArchived = true
                wordDao.merge(word)
            }
    }

    override fun unarchive(id: Long) {
        this.get(id)
            ?.let { word ->
                word.isArchived = false
                wordDao.merge(word)
            }
    }

    override fun resetTimesShown(id: Long) {
        this.get(id)
            ?.let { word ->
                word.timesShown = 0
                wordDao.merge(word)
            }
    }

    override fun saveAudioUrlAndTranscription(
        wordId: Long,
        audioUrl: String?,
        transcription: String?
    ) {
        get(wordId)?.let { neededWord ->
            audioUrl?.let { neededWord.audioUrl = it }
            transcription?.let { neededWord.transcription = it }
            this.update(neededWord)
        }
    }

    private fun findAllNonArchived(): List<Word> = wordDao.findAll().filter { it.isArchived.not() }

    private fun getReadyForTrainingWords() = wordDao.findAll()
        .filter { word -> word.isArchived.not() }
        .filter { word -> word.isHard().not() }
        .filter { word -> word.targetDate.time < System.currentTimeMillis() }
}



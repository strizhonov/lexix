package com.strizhonovapps.lexixapp.service

import com.strizhonovapps.lexixapp.dao.WordDao
import com.strizhonovapps.lexixapp.dao.WordDaoFilter
import com.strizhonovapps.lexixapp.dao.allActive
import com.strizhonovapps.lexixapp.dao.allAvailable
import com.strizhonovapps.lexixapp.model.AllowedWordCardSide
import com.strizhonovapps.lexixapp.model.HARD_LEVEL_COEF
import com.strizhonovapps.lexixapp.model.INITIAL_WORD_LEVEL
import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.model.TrainingType
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.view.wordlist.WordsBatch
import java.io.InputStream
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WordServiceImpl @Inject constructor(
    private val wordDao: WordDao,
    private val wordsParser: WordsParser,
    private val freezeTimeDefiner: WordFreezeTimeDefiner,
) : WordService {

    private val nativeWordsDelimiters = arrayOf(", ", "; ")
    private val unknownWordLevelReduceValue = 2

    override fun add(word: Word): Long = wordDao.save(word)
    override fun get(id: Long) = wordDao.get(id)
    override fun update(word: Word) = wordDao.merge(word)
    override fun delete(id: Long) = wordDao.delete(id)
    override fun findAll(filter: WordDaoFilter?, batch: WordsBatch?, orderByIdDesc: Boolean?) =
        wordDao.findAll(filter, batch, orderByIdDesc)

    override fun erase() = wordDao.erase()
    override fun count(filter: WordDaoFilter?): Long = wordDao.count(filter)

    override fun findDuplicatesByName(): List<Word> =
        findAll()
            .groupBy({ word -> word.name + word.tag }, { word -> word })
            .filter { entry -> entry.value.size > 1 }
            .flatMap { entry -> entry.value }
            .sortedBy { word -> word.name }

    override fun findDuplicatesByTranslation(): List<Word> =
        findAll()
            .flatMap(::mapToPairOfWordTranslationAndWord)
            .groupBy({ entry -> entry.first }, { entry -> entry.second })
            .filter { entry -> entry.value.distinct().size > 1 }
            .flatMap { entry -> entry.value }
            .distinct()

    override fun getCountOfReadyForTrainingWords(): Int = getReadyForTrainingWords().size

    override fun getCurrentWordAndCountOfActiveWords(
        trainingType: TrainingType?,
        wordToExclude: Word?
    ): Pair<Word?, Int> {
        val words = getReadyForTrainingWords(
            trainingType = trainingType,
            wordToExclude = wordToExclude
        )

        return Pair(
            words.minByOrNull(Word::targetDate),
            words.size
        )
    }

    override fun findAllNames(): List<String> = wordDao.findAllNames()

    override fun getAverageExpectedWordsAndCountOfActiveWords(): Pair<Int, Int> {
        val allAvailable = findAll(allAvailable)
        val averageExpectedWordsPerDay = getAverageExpectedWordsPerDay(allAvailable)
        val activeWords = getReadyForTrainingWords(allAvailable).size
        return Pair(averageExpectedWordsPerDay, activeWords)
    }

    override fun processKnown(word: Word) {
        word.timesShown = word.timesShown.inc()
        word.level = word.level.inc()
        word.setModificationDate(LocalDateTime.now())

        val wordsTargetDates = wordDao.findAllTargetDatesForNonArchived()
        val freezeTimeMs = freezeTimeDefiner.define(word.level, wordsTargetDates)
        val targetDate = LocalDateTime.now().plus(freezeTimeMs, ChronoUnit.MILLIS)
        word.setTargetDate(targetDate)
        wordDao.merge(word)
    }

    override fun processUnknown(word: Word) {
        word.timesShown = word.timesShown.inc()
        word.level = 1.coerceAtLeast(word.level - unknownWordLevelReduceValue)
        word.setModificationDate(LocalDateTime.now())

        val wordsTargetDates = wordDao.findAllTargetDatesForNonArchived()
        val freezeTimeMs = freezeTimeDefiner.define(word.level, wordsTargetDates)
        val targetDate = LocalDateTime.now().plus(freezeTimeMs, ChronoUnit.MILLIS)
        word.setTargetDate(targetDate)
        wordDao.merge(word)
    }

    override fun skipWord(word: Word) {
        word.setModificationDate(LocalDateTime.now())

        val wordsTargetDates = wordDao.findAllTargetDatesForNonArchived()
        val freezeTimeMs = freezeTimeDefiner.define(word.level, wordsTargetDates)
        val targetDate = LocalDateTime.now().plus(freezeTimeMs, ChronoUnit.MILLIS)
        word.setTargetDate(targetDate)
        wordDao.merge(word)
    }

    override fun resetClosestWordsDates(count: Int) =
        findAll(allAvailable)
            .sortedBy(Word::targetDate)
            .take(count)
            .forEach { word ->
                word.setTargetDate(LocalDateTime.now())
                word.setModificationDate(LocalDateTime.now())
                wordDao.merge(word)
            }

    override fun saveAllFromStream(
        inputStream: InputStream,
        separator: String,
        studyLanguage: SupportedLanguage
    ) = wordsParser.parse(inputStream, separator, studyLanguage).map(wordDao::merge)

    override fun resetProgress() {
        val allNonArchivedIds = findAll(WordDaoFilter(archived = false)).map { it.id }
        wordDao.resetProgress(allNonArchivedIds)
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

    override fun uncheckHard(id: Long) {
        this.get(id)
            ?.let { word ->
                word.reset()

                val wordsTargetDates = wordDao.findAllTargetDatesForNonArchived()
                val freezeTimeMs = freezeTimeDefiner.define(word.level, wordsTargetDates)
                word.setTargetDate(LocalDateTime.now().plus(freezeTimeMs, ChronoUnit.MILLIS))
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

    private fun getAverageExpectedWordsPerDay(availableWords: List<Word>): Int =
        availableWords
            .map { word ->
                (1).div(word.level.times(freezeTimeDefiner.baseIncreaseCoef))
            }
            .reduceOrNull { a, b -> a + b }
            ?.roundToInt()
            ?: 0

    private fun getReadyForTrainingWords(
        src: List<Word>? = null,
        trainingType: TrainingType? = null,
        wordToExclude: Word? = null
    ) =
        (src?.filter { word -> !word.isArchived }
            ?.filter { word -> !word.isHard() }
            ?.filter { word -> word.getTargetDate().isBefore(LocalDateTime.now()) }
            ?: wordDao.findAll(allActive))
            .filter { word -> !word.same(wordToExclude) }
            .filter { word ->
                trainingType?.let { type ->
                    doesTrainingTypeFitAllowedWordSide(type, word.allowedWordCardSide)
                } ?: true
            }

    private fun mapToPairOfWordTranslationAndWord(word: Word) =
        (word.translation?.split(*nativeWordsDelimiters) ?: emptyList())
            .map { translation ->
                Pair(
                    translation.trim().lowercase() + word.tag,
                    word
                )
            }

    private fun doesTrainingTypeFitAllowedWordSide(
        trainingType: TrainingType,
        allowedWordCardSide: AllowedWordCardSide
    ): Boolean {
        val listOfAllowedTrainings = when (allowedWordCardSide) {
            AllowedWordCardSide.STUDY -> listOf(TrainingType.STUDY_TO_NATIVE, TrainingType.MIXED)
            AllowedWordCardSide.NATIVE -> listOf(TrainingType.NATIVE_TO_STUDY, TrainingType.MIXED)
            AllowedWordCardSide.ALL -> TrainingType.values().toList()
        }
        return listOfAllowedTrainings.contains(trainingType)
    }

    companion object WordAddon {

        @JvmStatic
        @Suppress("BooleanMethodIsAlwaysInverted")
        fun Word.isHard() = timesShown.toDouble().div(level.coerceAtLeast(1)) >= HARD_LEVEL_COEF

        @JvmStatic
        fun Word.reset() {
            level = INITIAL_WORD_LEVEL
            timesShown = 0
            setTargetDate(LocalDateTime.now())
            setModificationDate(LocalDateTime.now())
        }
    }

}





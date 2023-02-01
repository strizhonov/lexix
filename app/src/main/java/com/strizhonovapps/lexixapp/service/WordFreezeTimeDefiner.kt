package com.strizhonovapps.lexixapp.service

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Random
import java.util.SortedSet
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordFreezeTimeDefiner @Inject constructor() {

    val baseIncreaseCoef = 1.5
    private val random = Random()
    private val wordsPerDayThreshold = 170

    fun define(newLevel: Int, wordsTargetDates: List<LocalDateTime>): Long {
        val levelBasedKnowledgeCoef = newLevel
            .times(TimeUnit.DAYS.toMillis(1))
            .toDouble()
            .times(baseIncreaseCoef)
            .toLong()

        val baseFreezeTimeMs = shiftFreezeTimeIfThereAreLotOfWordsAtThatPeriod(
            wordsTargetDates,
            levelBasedKnowledgeCoef
        )
        return baseFreezeTimeMs + randomDelta(baseFreezeTimeMs)
    }

    fun getFreezePeriods(wordDates: List<LocalDateTime>): List<Int> {
        val now = LocalDateTime.now()
        return wordDates.map { wordTargetDate ->
            if (wordTargetDate.isBefore(now)) return@map 0

            Integer.max(
                0,
                ChronoUnit.DAYS.between(now, wordTargetDate).toInt()
            ).plus(1)
        }
    }

    private fun wordFreezeTimeDays(wordDates: List<LocalDateTime>): SortedSet<Pair<Int, Int>> {
        val wholeDaysBeforeTargetDate = this.getFreezePeriods(wordDates)
        val maxDay = wholeDaysBeforeTargetDate.maxOrNull() ?: 0
        val counts = wholeDaysBeforeTargetDate
            .groupingBy { it }
            .eachCount()
            .toMutableMap()

        repeat(maxDay) { idx ->
            counts.putIfAbsent(idx + 1, 0)
        }

        return counts
            .map { (level, count) -> Pair(level, count) }
            .toSortedSet(Comparator.comparingInt { it.first })
    }

    private fun randomDelta(baseFreezeTimeMs: Long, dispersion: Double = 20.0): Int {
        val randomDelta = ((dispersion / 100) * baseFreezeTimeMs)
            .let { doubleValue ->
                if (random.nextBoolean()) -doubleValue
                else doubleValue
            }
        val randomShiftBase = ((dispersion / 100) * baseFreezeTimeMs).toInt()
        val randomShift = random
            .nextInt(if (randomShiftBase == 0) 1 else randomShiftBase)
            .let { doubleValue ->
                if (random.nextBoolean()) -doubleValue
                else doubleValue
            }
        return randomShift.plus(randomDelta.toInt())
    }

    private fun shiftFreezeTimeIfThereAreLotOfWordsAtThatPeriod(
        wordsTargetDates: List<LocalDateTime>,
        freezeTimeMs: Long
    ): Long {
        if (wordsTargetDates.isEmpty()) return freezeTimeMs
        val wordAppearanceDays = wordFreezeTimeDays(wordsTargetDates)

        var isFirstDecentDay = true
        for (dayAndCount in wordAppearanceDays) {
            if (dayAndCount.first < TimeUnit.MILLISECONDS.toDays(freezeTimeMs)) {
                continue
            }
            if (dayAndCount.second > wordsPerDayThreshold) {
                isFirstDecentDay = false
                continue
            }
            return if (isFirstDecentDay) freezeTimeMs
            else TimeUnit.DAYS.toMillis(dayAndCount.first.plus(1).toLong())
        }

        val dayToPlaceWord = (wordAppearanceDays.maxOfOrNull { it.first } ?: 0).plus(1)
        return TimeUnit.DAYS.toMillis(dayToPlaceWord.toLong())
    }
}
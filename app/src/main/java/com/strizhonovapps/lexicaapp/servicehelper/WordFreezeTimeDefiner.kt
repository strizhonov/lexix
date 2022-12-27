package com.strizhonovapps.lexicaapp.servicehelper

import android.util.Log
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordFreezeTimeDefiner @Inject constructor() {

    private val random = Random()
    private val oneDayInMs = 24L * 60 * 60 * 1000
    private val dispersionValuePercents = 20.0

    fun define(newLevel: Int, activeWords: Int): Long {
        val intermediateFreezeTime = newLevel
            .times(oneDayInMs)
            .toDouble()
            .times(defineLevelBasedCoef(activeWords))
            .toLong()

        val freezeTimeMs = intermediateFreezeTime + randomDelta(
            intermediateFreezeTime,
            dispersionValuePercents
        )

        Log.d(
            "FREEZE TIME",
            "Freeze time for new level '$newLevel' is '$freezeTimeMs' days."
        )
        return freezeTimeMs
    }

    private fun randomDelta(
        intermediateFreezeTime: Long,
        @Suppress("SameParameterValue") dispersion: Double
    ): Int {
        val randomLimit = ((dispersion / 50.0) * intermediateFreezeTime).toInt()
        val randomDelta =
            ((dispersion / 100) * intermediateFreezeTime).let { if (random.nextBoolean()) -it else it }
        return random.nextInt(if (randomLimit == 0) 1 else randomLimit).plus(randomDelta.toInt())
    }

    private fun defineLevelBasedCoef(activeWords: Int): Double {
        return when (activeWords) {
            in 0..50 -> 1.1
            in 51..100 -> 1.2
            else -> 1.3
        }
    }

}
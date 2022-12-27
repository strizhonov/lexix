package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.servicehelper.isFirstDayAfterSecondDay
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class LangUtilsKtTest {

    @Test
    fun `first day is after second day`() {
        assertTrue(
            isFirstDayAfterSecondDay(
                getDate(2022, 1, 16),
                getDate(2022, 1, 15)
            )
        )
        assertTrue(
            isFirstDayAfterSecondDay(
                getDate(2022, 1, 16),
                getDate(2021, 12, 30)
            )
        )
    }

    @Test
    fun `second day is after second day`() {
        val firstDay = getDate(2021, 2, 15)
        val anotherDay = getDate(2026, 8, 16)
        assertFalse(isFirstDayAfterSecondDay(firstDay, anotherDay))
    }

    @Test
    fun `equal days`() {
        assertFalse(
            isFirstDayAfterSecondDay(
                getDate(2021, 2, 15),
                getDate(2021, 2, 15)
            )
        )
        assertFalse(
            isFirstDayAfterSecondDay(
                getDate(2021, 2, 15, 1),
                getDate(2021, 2, 15, 12)
            )
        )
        assertFalse(
            isFirstDayAfterSecondDay(
                getDate(2021, 2, 15, 12),
                getDate(2021, 2, 15, 1)
            )
        )
    }

    private fun getDate(year: Int, month: Int, day: Int, hour: Int = 0): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
        }
    }

}
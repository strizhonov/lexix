package com.strizhonovapps.lexicaapp.servicehelper

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

fun getLinesFromInputStream(inputStream: InputStream?) =
    getStringFromInputStream(inputStream).split("\n")

fun getStringFromInputStream(inputStream: InputStream?): String {
    BufferedReader(InputStreamReader(inputStream)).use { reader ->
        val builder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
            builder.append("\n")
        }
        return builder.toString()
    }
}

fun isFirstDayAfterSecondDay(first: Calendar, second: Calendar): Boolean {
    if (first.timeInMillis <= second.timeInMillis) return false
    if (first.get(Calendar.YEAR) > second.get(Calendar.YEAR)) return true
    return first.get(Calendar.DAY_OF_YEAR) >= second.get(Calendar.DAY_OF_YEAR) + 1
}
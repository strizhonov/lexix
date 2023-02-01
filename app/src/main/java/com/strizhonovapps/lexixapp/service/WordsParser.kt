package com.strizhonovapps.lexixapp.service

import android.util.Log
import com.strizhonovapps.lexixapp.model.INITIAL_WORD_LEVEL
import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.model.Word
import com.strizhonovapps.lexixapp.util.getLinesFromInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordsParser @Inject constructor() {

    private val multilineWordSeparator = "```"

    fun parse(inputStream: InputStream, separator: String, studyLanguage: SupportedLanguage) =
        map(getLinesFromInputStream(inputStream), separator, studyLanguage)

    private fun map(
        lines: List<String>,
        separator: String,
        studyLanguage: SupportedLanguage
    ): List<Word> {
        val linesPerItem = ArrayList<List<String>>()
        var multilineOn = false
        var currentLines = ArrayList<String>()
        for (line in lines) {
            if (line == multilineWordSeparator) {
                multilineOn = !multilineOn
            } else {
                currentLines.add(line)
            }
            if (!multilineOn) {
                linesPerItem.add(currentLines)
                currentLines = ArrayList()
            }
        }
        return linesPerItem.mapNotNull { oneItemLines ->
            if (oneItemLines.size == 1) {
                parseLine(oneItemLines[0], separator, studyLanguage)
            } else {
                createFromMultiline(oneItemLines)
            }
        }
    }

    private fun parseLine(
        line: String,
        separator: String,
        studyLanguage: SupportedLanguage
    ): Word? {
        if (!line.contains(separator)) {
            Log.w(this.javaClass.simpleName, "Separator not found, skipping current line")
            return null
        }
        val splittedLine = line.split(separator)
        if (splittedLine.size < 2) return null

        val wordName = splittedLine[0].trim()
        val translation = splittedLine[1].trim()
        val lvl = if (splittedLine.size > 2) getLvl(splittedLine) else INITIAL_WORD_LEVEL
        val tag =
            if (splittedLine.size > 3) splittedLine[3]
            else studyLanguage.name

        return Word(name = wordName, translation = translation, level = lvl, tag = tag)
    }

    private fun createFromMultiline(oneItemLines: List<String>) =
        Word(
            name = oneItemLines[0],
            translation = oneItemLines.subList(1, oneItemLines.size)
                .joinToString(System.lineSeparator())
        )

    private fun getLvl(splittedLine: List<String>): Int {
        return try {
            splittedLine[2].toInt()
        } catch (e: NumberFormatException) {
            INITIAL_WORD_LEVEL
        }
    }

}
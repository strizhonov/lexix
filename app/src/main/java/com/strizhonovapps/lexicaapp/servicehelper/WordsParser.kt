package com.strizhonovapps.lexicaapp.servicehelper

import android.util.Log
import com.strizhonovapps.lexicaapp.model.INITIAL_WORD_LEVEL
import com.strizhonovapps.lexicaapp.model.Word
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordsParser @Inject constructor() {

    private val multilineWordSeparator = "```"

    fun parse(inputStream: InputStream, separator: String) =
        map(getLinesFromInputStream(inputStream), separator)

    private fun map(lines: List<String>, separator: String): List<Word> {
        val linesOfWords = ArrayList<List<String>>()
        var multilineOn = false
        var currentLines = ArrayList<String>()
        for (line in lines) {
            if (line == multilineWordSeparator) {
                multilineOn = !multilineOn
            } else {
                currentLines.add(line)
            }
            if (!multilineOn) {
                linesOfWords.add(currentLines)
                currentLines = ArrayList()
            }
        }
        return linesOfWords.mapNotNull { wordLines ->
            if (wordLines.size == 1) {
                parseLine(wordLines[0], separator)
            } else {
                Word(
                    name = wordLines[0],
                    translation = wordLines.subList(1, wordLines.size).joinToString("\n")
                )
            }
        }
    }

    private fun parseLine(line: String, separator: String): Word? {
        if (!line.contains(separator)) {
            Log.w(this.javaClass.simpleName, "Separator not found, skipping current line")
            return null
        }
        val splittedLine = line.split(separator)
        if (splittedLine.size < 2) return null
        val wordName = splittedLine[0]
        val translation = splittedLine[1]
        val lvl = if (splittedLine.size > 2) getLvl(splittedLine) else INITIAL_WORD_LEVEL
        val tag = if (splittedLine.size > 3) splittedLine[3] else null
        return Word(name = wordName, translation = translation, level = lvl, tag = tag)
    }

    private fun getLvl(splittedLine: List<String>): Int {
        return try {
            splittedLine[2].toInt()
        } catch (e: NumberFormatException) {
            INITIAL_WORD_LEVEL
        }
    }

}
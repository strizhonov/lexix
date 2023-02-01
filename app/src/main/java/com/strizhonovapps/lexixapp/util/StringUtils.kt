package com.strizhonovapps.lexixapp.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

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
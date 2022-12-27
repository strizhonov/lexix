package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.servicehelper.WordFreezeTimeDefiner
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration

internal class WordFreezeTimeDefinerTest {

    private val freezeTimeDefiner = WordFreezeTimeDefiner()

    @Test
    @Disabled("Visual test")
    fun `level change`() {
        (1..100)
            .map { Pair(it, Duration.ofMillis(freezeTimeDefiner.define(it, 50)).toDays()) }
            .forEach {
                print("${it.first}---")
                repeat(it.second.toInt()) { print("*") }
                println()
            }
    }

    @Test
    @Disabled("Visual test")
    fun `active words change`() {
        (1..10).union(55..65).union(120..130)
            .map { Pair(it, Duration.ofMillis(freezeTimeDefiner.define(15, it)).toDays()) }
            .forEach {
                print("${it.first}---")
                repeat(it.second.toInt()) { print("*") }
                println()
            }
    }
}
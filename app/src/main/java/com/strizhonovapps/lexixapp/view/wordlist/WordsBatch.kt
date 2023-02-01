package com.strizhonovapps.lexixapp.view.wordlist

data class WordsBatch(var offset: Int, val pageSize: Int = 100) {

    fun reset() {
        offset = 0
    }

    fun end() = offset.plus(pageSize)

    fun next() = WordsBatch(offset.plus(pageSize))

}
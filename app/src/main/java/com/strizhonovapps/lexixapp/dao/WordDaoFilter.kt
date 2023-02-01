package com.strizhonovapps.lexixapp.dao

data class WordDaoFilter(
    val hard: Boolean? = null,
    val archived: Boolean? = null,
    val targetDateBeforeNow: Boolean? = null,
    val offsetAndSize: Pair<Int, Int>? = null,
)

val allAvailable = WordDaoFilter(hard = false, archived = false)
val allHard = WordDaoFilter(hard = true)
val allArchived = WordDaoFilter(archived = true)
val allActive = WordDaoFilter(archived = false, hard = false, targetDateBeforeNow = true)
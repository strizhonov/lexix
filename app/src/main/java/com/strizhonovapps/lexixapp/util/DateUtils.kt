package com.strizhonovapps.lexixapp.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun now() = Instant.now().toEpochMilli()

fun toLocalDateTime(timestamp: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

fun toLocalDateTime(date: Date): LocalDateTime =
    LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())

fun toDate(timestamp: Long): Date = Date(timestamp)

fun toDate(localDateTime: LocalDateTime): Date =
    Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

fun toTimestamp(date: Date): Long = date.time
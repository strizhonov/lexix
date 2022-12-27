package com.strizhonovapps.lexicaapp.dao

import com.strizhonovapps.lexicaapp.model.Word

interface CrudDao<T> {
    fun save(entity: T): Long
    fun get(id: Long): T?
    fun merge(entity: Word): Long
    fun delete(id: Long)
    fun findAll(): List<T>
    fun erase(): Int
    fun size(): Long
}

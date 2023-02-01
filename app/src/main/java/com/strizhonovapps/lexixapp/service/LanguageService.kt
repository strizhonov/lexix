package com.strizhonovapps.lexixapp.service

import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.model.WordMetaData

interface LanguageService {
    suspend fun getAllTranslations(queryString: String): List<String>
    suspend fun translateFromNative(nativeWord: String): String?
    suspend fun translateFromEnToStudyLang(word: String): String?
    suspend fun getWordMetadata(wordId: Long, wordName: String): WordMetaData?
    suspend fun getEnWord(word: String): String?
    fun getStudyLanguage(): SupportedLanguage
    fun getNativeLanguage(): SupportedLanguage
    fun setStudyLanguage(lang: SupportedLanguage)
    fun setNativeLanguage(lang: SupportedLanguage)
}
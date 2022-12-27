package com.strizhonovapps.lexicaapp.service

import com.strizhonovapps.lexicaapp.model.SupportedLanguage
import com.strizhonovapps.lexicaapp.model.WordMetaData

interface WordOperationsService {
    fun getSupportedByYdxStudyLangsForNativeLang(nativeLangCode: String): Array<SupportedLanguage>?
    fun getAllTranslations(queryString: String): List<String>
    fun getTranslationFromNative(nativeWord: String): String?
    fun translateFromEnToStudyLang(word: String): String?
    fun downloadWordMetadata(wordName: String, wordId: Long): WordMetaData?
    fun getStudyLanguage(): SupportedLanguage
    fun getNativeLanguage(): SupportedLanguage
    fun setStudyLanguage(lang: SupportedLanguage)
    fun setNativeLanguage(lang: SupportedLanguage)
}
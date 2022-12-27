package com.strizhonovapps.lexicaapp.servicehelper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.strizhonovapps.lexicaapp.model.SupportedLanguage
import com.strizhonovapps.lexicaapp.model.YndxDefinition
import com.strizhonovapps.lexicaapp.model.YndxDictionaryResponse
import com.strizhonovapps.lexicaapp.model.YndxTranslation
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

const val YANDEX_DICT_URL =
    "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=%s&amp;lang=%s&amp;text=%s"

const val YANDEX_LANGS_URL = "https://dictionary.yandex.net/api/v1/dicservice.json/getLangs?key=%s"

@Singleton
class YdxLangApiDataProvider @Inject constructor(
    private val requestSender: RequestSender,
    @Named("yndxDictionaryApiKey") val yandexApiKey: String,
) {

    fun getTranslation(
        queryWord: String,
        from: SupportedLanguage,
        to: SupportedLanguage,
    ): String? = getAllTranslations(queryWord, from, to).firstOrNull()

    fun getAllTranslations(
        queryWord: String,
        from: SupportedLanguage,
        to: SupportedLanguage,
    ): List<String> {
        val requestURL = String.format(
            YANDEX_DICT_URL,
            yandexApiKey,
            "${from.yndxLangKey}-${to.yndxLangKey}",
            queryWord
        )
        val jsonResponse = requestSender.sendGetRequest(requestURL)
        return Gson().fromJson(jsonResponse, YndxDictionaryResponse::class.java)
            .def
            .flatMap(YndxDefinition::tr)
            .mapNotNull(YndxTranslation::text)
    }

    fun getSupportedLangs(): Map<SupportedLanguage, List<SupportedLanguage>> {
        val listOfStringsType = object : TypeToken<List<String>>() {}.type
        val response =
            requestSender.sendGetRequest(String.format(YANDEX_LANGS_URL, yandexApiKey))
        return Gson().fromJson<List<String>>(response, listOfStringsType)
            .map(::getLanguagesFromStringPair)
            .filter { (native, study) -> native != null && study != null }
            .groupBy({ (native, _) -> native!! }, { (_, study) -> study!! })
    }

    private fun getLanguagesFromStringPair(langsPair: String): Pair<SupportedLanguage?, SupportedLanguage?> {
        val nativeLanguageString = langsPair.split("-")[0]
        val studyLanguageString = langsPair.split("-")[1]
        val nativeLanguage = defineLangByYndxApiKey(nativeLanguageString)
        val studyLanguage = defineLangByYndxApiKey(studyLanguageString)
        return Pair(nativeLanguage, studyLanguage)
    }

    private fun defineLangByYndxApiKey(key: String) =
        SupportedLanguage.values().filter { it.yndxLangKey == key }.randomOrNull()

}
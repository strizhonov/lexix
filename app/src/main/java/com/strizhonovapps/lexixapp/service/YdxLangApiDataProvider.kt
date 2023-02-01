package com.strizhonovapps.lexixapp.service

import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.model.YndxDefinition
import com.strizhonovapps.lexixapp.model.YndxDictionaryResponse
import com.strizhonovapps.lexixapp.model.YndxTranslation
import com.strizhonovapps.lexixapp.util.RequestSender
import com.strizhonovapps.lexixapp.util.getStringFromInputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


const val YANDEX_DICT_URL =
    "https://dictionary.yandex.net/api/v1/dicservice.json/lookup?key=%s&lang=%s&text=%s"

private const val YANDEX_LANGS_FILE = "yandex-supported-langs.json"

@Singleton
class YdxLangApiDataProvider @Inject constructor(
    assetManager: AssetManager,
    private val requestSender: RequestSender,
    @Named("yndxDictionaryApiKey") val yandexApiKey: String,
) {

    val supportedLangs: Map<SupportedLanguage, List<SupportedLanguage>>

    init {
        val inputStream = assetManager.open(YANDEX_LANGS_FILE)
        val fileContent = getStringFromInputStream(inputStream)
        val listOfStringsType = object : TypeToken<List<String>>() {}.type
        supportedLangs = Gson().fromJson<List<String>>(fileContent, listOfStringsType)
            .map(::getLanguagesFromStringPair)
            .filter { (native, study) -> native != null && study != null }
            .groupBy({ (native, _) -> native!! }, { (_, study) -> study!! })
    }

    suspend fun getTranslation(
        queryWord: String,
        from: SupportedLanguage,
        to: SupportedLanguage,
    ): String? = getAllTranslations(queryWord, from, to).firstOrNull()

    suspend fun getAllTranslations(
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

    private fun getLanguagesFromStringPair(langsPair: String): Pair<SupportedLanguage?, SupportedLanguage?> {
        val nativeLanguageString = langsPair.split("-")[0]
        val studyLanguageString = langsPair.split("-")[1]
        val nativeLanguage = defineLangByYndxApiKey(nativeLanguageString)
        val studyLanguage = defineLangByYndxApiKey(studyLanguageString)
        return Pair(nativeLanguage, studyLanguage)
    }

    private fun defineLangByYndxApiKey(key: String) =
        SupportedLanguage.values().filter { lang -> lang.yndxLangKey == key }.randomOrNull()

}
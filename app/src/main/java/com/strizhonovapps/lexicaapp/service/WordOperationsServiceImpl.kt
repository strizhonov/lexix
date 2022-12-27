package com.strizhonovapps.lexicaapp.service

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.util.Log
import com.strizhonovapps.lexicaapp.model.LanguageType
import com.strizhonovapps.lexicaapp.model.LeoWordData
import com.strizhonovapps.lexicaapp.model.SupportedLanguage
import com.strizhonovapps.lexicaapp.model.WordMetaData
import com.strizhonovapps.lexicaapp.servicehelper.LinguaLeoApiProvider
import com.strizhonovapps.lexicaapp.servicehelper.YdxLangApiDataProvider
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordOperationsServiceImpl @Inject constructor(
    private val ydxLangApiDataProvider: YdxLangApiDataProvider,
    private val linguaLeoApiProvider: LinguaLeoApiProvider,
    private val preferences: SharedPreferences,
    private val imageService: ImageService,
    private val wordService: WordService,
) : WordOperationsService {

    private var supportedStudyLangsForNativeLang: Map<SupportedLanguage, List<SupportedLanguage>>? =
        null

    init {
        tryInitSupportedLangsMap()
    }

    override fun getSupportedByYdxStudyLangsForNativeLang(nativeLangCode: String): Array<SupportedLanguage>? {
        if (supportedStudyLangsForNativeLang == null) tryInitSupportedLangsMap()
        val nativeLang = defineLangByCode(nativeLangCode)
        return supportedStudyLangsForNativeLang?.get(nativeLang)?.toTypedArray()
    }

    override fun getAllTranslations(queryString: String): List<String> {
        val from = getStudyLanguage()
        val to = getNativeLanguage()
        return ydxLangApiDataProvider.getAllTranslations(queryString, from, to)
    }

    override fun getTranslationFromNative(nativeWord: String): String? =
        ydxLangApiDataProvider.getTranslation(nativeWord, getNativeLanguage(), getStudyLanguage())

    override fun downloadWordMetadata(wordName: String, wordId: Long): WordMetaData? {
        try {
            val leoValidPictureData = linguaLeoApiProvider.getWordData(
                wordName,
                getStudyLanguage().leoLangKey,
                getNativeLanguage().leoLangKey
            )
            val image = leoValidPictureData.picUrl
                ?.let { picUrl -> if (picUrl == "") null else picUrl }
                ?.let(::URL)
                ?.let(URL::openStream)
                ?.let(BitmapFactory::decodeStream)
                ?: getEnWord(wordName)?.let(imageService::downloadImage)

            val leoValidSoundData = getLeoSoundData(wordName)
            val transcription = leoValidSoundData.transcription
            val soundUrl = leoValidSoundData.soundUrl

            image?.let { img ->
                imageService.saveImage(wordId, img)
            }

            wordService.saveAudioUrlAndTranscription(
                wordId,
                soundUrl,
                transcription
            )

            return WordMetaData(image, transcription, soundUrl)
        } catch (e: Exception) {
            Log.w(
                this.javaClass.name,
                "Unable to download metadata by name '${wordName}', exception ${e.message}"
            )
            return null
        }
    }

    override fun getStudyLanguage(): SupportedLanguage {
        val studyLangCode =
            preferences.getString(LanguageType.STUDY_LANGUAGE.toString(), SupportedLanguage.EN.name)
        return defineLangByCode(studyLangCode!!)
    }

    override fun getNativeLanguage(): SupportedLanguage {
        val nativeLangCode = preferences.getString(
            LanguageType.NATIVE_LANGUAGE.toString(),
            SupportedLanguage.RU.name
        )
        return defineLangByCode(nativeLangCode!!)
    }

    override fun setStudyLanguage(lang: SupportedLanguage) {
        preferences.edit().apply {
            putString(
                LanguageType.STUDY_LANGUAGE.toString(),
                lang.name
            )
            apply()
        }
    }

    override fun setNativeLanguage(lang: SupportedLanguage) {
        preferences.edit().apply {
            putString(
                LanguageType.NATIVE_LANGUAGE.toString(),
                lang.name
            )
            apply()
        }
    }

    override fun translateFromEnToStudyLang(word: String): String? {
        val studyLang = getStudyLanguage()
        if (studyLang == SupportedLanguage.EN) return word

        val supportedLangs =
            getSupportedByYdxStudyLangsForNativeLang(SupportedLanguage.EN.yndxLangKey)
        if (supportedLangs?.contains(studyLang) == true) {
            return try {
                ydxLangApiDataProvider
                    .getAllTranslations(word, SupportedLanguage.EN, studyLang)
                    .firstOrNull()
            } catch (e: Exception) {
                Log.w(
                    this.javaClass.name,
                    "Can't translate '${word}', exception ${e.message}"
                )
                null
            }
        }
        return linguaLeoApiProvider
            .getWord(word, SupportedLanguage.EN.leoLangKey, getStudyLanguage().leoLangKey)
            .translate
            .firstOrNull()
            ?.value
    }

    private fun getEnWord(word: String): String? {
        if (getStudyLanguage() == SupportedLanguage.EN) return word

        return try {
            translateTo(word, SupportedLanguage.EN)
        } catch (e: Exception) {
            Log.w(
                this.javaClass.name,
                "Can't translate '${word}', exception ${e.message}"
            )
            null
        }
    }

    private fun getLeoSoundData(wordName: String): LeoWordData {
        // Lingua leo preparing request
        linguaLeoApiProvider.getWordData(
            wordName,
            getStudyLanguage().leoLangKey,
            getStudyLanguage().leoLangKey
        )
        return linguaLeoApiProvider.getWordData(
            wordName,
            getStudyLanguage().leoLangKey,
            getStudyLanguage().leoLangKey
        )
    }

    private fun translateTo(word: String, to: SupportedLanguage): String? =
        ydxLangApiDataProvider.getAllTranslations(word, getStudyLanguage(), to).firstOrNull()

    private fun defineLangByCode(code: String) =
        SupportedLanguage.valueOf(code.uppercase(Locale.ROOT))

    private fun tryInitSupportedLangsMap() =
        try {
            supportedStudyLangsForNativeLang = ydxLangApiDataProvider.getSupportedLangs()
        } catch (e: ExecutionException) {
            // init map later
        } catch (e: TimeoutException) {
            // init map later
        }
}
package com.strizhonovapps.lexixapp.service

import android.util.Log
import com.strizhonovapps.lexixapp.model.LeoWordData
import com.strizhonovapps.lexixapp.model.SupportedLanguage
import com.strizhonovapps.lexixapp.model.WordMetaData
import com.strizhonovapps.lexixapp.util.PrefsDecorator
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageServiceImpl @Inject constructor(
    private val ydxLangApiDataProvider: YdxLangApiDataProvider,
    private val linguaLeoApiProvider: LinguaLeoApiProvider,
    private val preferences: PrefsDecorator,
) : LanguageService {

    override suspend fun getAllTranslations(queryString: String): List<String> {
        val from = getStudyLanguage()
        val to = getNativeLanguage()
        return ydxLangApiDataProvider.getAllTranslations(queryString, from, to)
    }

    override suspend fun translateFromNative(nativeWord: String): String? =
        ydxLangApiDataProvider.getTranslation(nativeWord, getNativeLanguage(), getStudyLanguage())

    override suspend fun getWordMetadata(wordId: Long, wordName: String): WordMetaData? {
        try {
            val leoValidPictureData = linguaLeoApiProvider.getWordData(
                wordName,
                getStudyLanguage().leoLangKey,
                getNativeLanguage().leoLangKey
            )
            val picUrl = leoValidPictureData.picUrl

            val leoValidSoundData = getLeoSoundData(wordName)
            val transcription = leoValidSoundData.transcription
            val soundUrl = leoValidSoundData.soundUrl

            return WordMetaData(wordId, picUrl, soundUrl, transcription)
        } catch (e: Exception) {
            Log.w(
                this.javaClass.name,
                "Unable to download metadata by name '${wordName}', exception ${e.message}"
            )
            return null
        }
    }

    override fun getStudyLanguage(): SupportedLanguage {
        val studyLangCode = preferences.getStudyLang() ?: SupportedLanguage.EN.name
        return defineLangByCode(studyLangCode)
    }

    override fun getNativeLanguage(): SupportedLanguage {
        val nativeLangCode = preferences.getNativeLang() ?: SupportedLanguage.RU.name
        return defineLangByCode(nativeLangCode)
    }

    override fun setStudyLanguage(lang: SupportedLanguage) {
        preferences.setStudyLang(lang.name)
    }

    override fun setNativeLanguage(lang: SupportedLanguage) {
        preferences.setNativeLang(lang.name)
    }

    override suspend fun translateFromEnToStudyLang(word: String): String? {
        val studyLang = getStudyLanguage()
        if (studyLang == SupportedLanguage.EN) return word

        val supportedLangs =
            getSupportedYdxStudyLangs(SupportedLanguage.EN.yndxLangKey)
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

    override suspend fun getEnWord(word: String): String? {
        if (getStudyLanguage() == SupportedLanguage.EN) return word

        return try {
            translate(word)
        } catch (e: Exception) {
            Log.w(
                this.javaClass.name,
                "Can't translate '${word}', exception ${e.message}"
            )
            null
        }
    }

    private fun getSupportedYdxStudyLangs(nativeLangCode: String): Array<SupportedLanguage>? {
        val nativeLang = defineLangByCode(nativeLangCode)
        return ydxLangApiDataProvider.supportedLangs[nativeLang]?.toTypedArray()
    }

    private suspend fun getLeoSoundData(wordName: String): LeoWordData {
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

    private suspend fun translate(
        word: String,
        to: SupportedLanguage = SupportedLanguage.EN
    ): String? =
        ydxLangApiDataProvider.getAllTranslations(word, getStudyLanguage(), to).firstOrNull()

    private fun defineLangByCode(code: String) =
        SupportedLanguage.valueOf(code.uppercase(Locale.ROOT))
}
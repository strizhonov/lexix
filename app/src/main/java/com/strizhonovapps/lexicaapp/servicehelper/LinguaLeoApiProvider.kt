package com.strizhonovapps.lexicaapp.servicehelper

import android.util.Log
import com.google.gson.Gson
import com.strizhonovapps.lexicaapp.model.LeoWord
import com.strizhonovapps.lexicaapp.model.LeoWordData
import com.strizhonovapps.lexicaapp.model.LeoWordRequest
import javax.inject.Inject
import javax.inject.Singleton

private const val API_URL = "https://api.lingualeo.com/"

@Singleton
class LinguaLeoApiProvider @Inject constructor(private val requestSender: RequestSender) {

    private val translatesRequestUrl = "${API_URL}gettranslates"

    fun getWordData(word: String, source: String, target: String): LeoWordData {
        val responseBody = requestSender.sendPostRequest(
            translatesRequestUrl,
            Gson().toJson(LeoWordRequest(word, source, target))
        )
        Log.d("LEO", "Request: word=$word, source=$source, target=$target, response: $responseBody")
        val leoWord = Gson().fromJson(responseBody, LeoWord::class.java)
        return LeoWordData(
            leoWord.sound_url,
            leoWord.transcription,
            leoWord.translate.maxByOrNull { it.votes }?.pic_url
        )
    }

    fun getWord(word: String, source: String, target: String): LeoWord {
        val responseBody = requestSender.sendPostRequest(
            translatesRequestUrl,
            Gson().toJson(LeoWordRequest(word, source, target))
        )
        Log.d("LEO", "Request: word=$word, source=$source, target=$target, response: $responseBody")
        return Gson().fromJson(responseBody, LeoWord::class.java)
    }

}
package com.strizhonovapps.lexixapp.util

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

val jsonMediaType = "application/json".toMediaTypeOrNull()

@Singleton
class RequestSender @Inject constructor() {

    suspend fun sendGetRequest(requestUrl: String) =
        Uri.parse(requestUrl).toString()
            .let { uri -> sendGetRequest(URL(uri)) }

    suspend fun sendPostRequest(requestUrl: String, jsonBody: String?) =
        Uri.parse(requestUrl).toString()
            .let { uri -> sendPostRequest(URL(uri), jsonBody) }

    private suspend fun sendGetRequest(requestUrl: URL, timeoutMs: Long = 3000): String {
        val client = OkHttpClient.Builder()
            .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url(requestUrl)
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request)
                .execute()
                .body
                .string()
        }
    }

    private suspend fun sendPostRequest(
        requestUrl: URL,
        body: String?,
        timeoutMs: Long = 3000
    ): String {
        val client = OkHttpClient.Builder()
            .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url(requestUrl)
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request)
                .execute()
                .body
                .string()
        }
    }

}
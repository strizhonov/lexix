package com.strizhonovapps.lexicaapp.servicehelper

import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestSender @Inject constructor() {

    fun sendGetRequest(requestUrl: String) =
        Uri.parse(requestUrl).toString()
            .let { uri -> sendGetRequest(URL(uri)) }

    fun sendPostRequest(requestUrl: String, jsonBody: String?) =
        Uri.parse(requestUrl).toString()
            .let { uri -> sendPostRequest(URL(uri), jsonBody) }

    private fun sendGetRequest(requestUrl: URL): String =
        Executors.newSingleThreadExecutor().submit(
            Callable {
                (requestUrl.openConnection() as? HttpURLConnection)?.run {
                    try {
                        connect()
                        getStringFromInputStream(inputStream)
                    } finally {
                        disconnect()
                    }
                }
            }
        )[5000, TimeUnit.MILLISECONDS]

    private fun sendPostRequest(requestUrl: URL, jsonBody: String?): String =
        Executors.newSingleThreadExecutor().submit(
            Callable {
                (requestUrl.openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    outputStream.use { os ->
                        jsonBody?.toByteArray(Charsets.UTF_8)?.let { os?.write(it, 0, it.size) }
                    }
                    try {
                        connect()
                        getStringFromInputStream(inputStream)
                    } finally {
                        disconnect()
                    }
                }
            }
        )[5000, TimeUnit.MILLISECONDS]

}
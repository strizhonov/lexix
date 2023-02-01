package com.strizhonovapps.lexixapp.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.strizhonovapps.lexixapp.model.UnsplashRootModel
import com.strizhonovapps.lexixapp.util.RequestSender
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

const val UNSPLASH_API_TEMPLATE =
    "https://api.unsplash.com/search/photos/?client_id=%s&query=%s&per_page=1"

@Singleton
class UnsplashApiProvider @Inject constructor(
    @Named("unsplashApiKey") private val clientId: String,
    private val requestSender: RequestSender,
) {

    suspend fun getImage(queryWord: String): Bitmap? {
        val requestUrl = String.format(UNSPLASH_API_TEMPLATE, clientId, queryWord)
        val unsplashJson = requestSender.sendGetRequest(requestUrl)
        return Gson().fromJson(unsplashJson, UnsplashRootModel::class.java)
            .let { rootModel -> rootModel.results.getOrNull(0)?.urls?.small }
            ?.let { imgUrl -> URL(imgUrl).openStream() }
            .let(BitmapFactory::decodeStream)
    }

}
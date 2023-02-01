package com.strizhonovapps.lexixapp.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.strizhonovapps.lexixapp.model.WordMetaData
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordMetadataFacadeService @Inject constructor() {

    @Inject
    lateinit var imageService: ImageService

    @Inject
    lateinit var wordService: WordService

    @Inject
    lateinit var languageService: LanguageService

    suspend fun saveMetadata(wordId: Long, wordName: String): Pair<Bitmap?, WordMetaData?> {
        val metadata = languageService.getWordMetadata(wordId, wordName) ?: return Pair(null, null)
        val image = metadata.picUrl
            ?.let { picUrl -> if (picUrl == "") null else picUrl }
            ?.let { picUrl -> URL(picUrl) }
            ?.let(URL::openStream)
            ?.let(BitmapFactory::decodeStream)
            ?: languageService.getEnWord(wordName)?.let { imageService.downloadImage(it) }

        image?.let { img ->
            imageService.saveImage(metadata.id, img)
        }

        wordService.saveAudioUrlAndTranscription(
            metadata.id,
            metadata.audioUrl,
            metadata.transcription
        )
        return Pair(image, metadata)
    }
}
package com.strizhonovapps.lexicaapp.di

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.strizhonovapps.lexicaapp.R
import com.strizhonovapps.lexicaapp.dao.*
import com.strizhonovapps.lexicaapp.service.ImageService
import com.strizhonovapps.lexicaapp.service.WordService
import com.strizhonovapps.lexicaapp.service.WordServiceImpl
import com.strizhonovapps.lexicaapp.servicehelper.UnsplashApiProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class BeanModule(private val context: Context) {

    @Provides
    fun provideWordService(service: WordServiceImpl): WordService = service

    @Provides
    fun provideWordDao(dao: WordDaoImpl): WordDao = WordCachingDao(dao)

    @Provides
    fun provideWordSuggestionDao(dao: WordSuggestionDaoImpl): WordSuggestionDao =
        WordSuggestionCachingDao(dao)

    @Provides
    fun providePreferences(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    fun provideImageService(unsplashApiProvider: UnsplashApiProvider): ImageService =
        ImageService(ContextWrapper(context), unsplashApiProvider)

    @Provides
    @Named("yndxDictionaryApiKey")
    fun provideYndxDictApiKey(): String = context.getString(R.string.yandex_dictionary_api_key)

    @Provides
    @Named("unsplashApiKey")
    fun provideUnsplashApiKey(): String = context.getString(R.string.unsplash_api_key)

}
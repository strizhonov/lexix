package com.strizhonovapps.lexixapp.di

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import com.strizhonovapps.lexixapp.R
import com.strizhonovapps.lexixapp.dao.*
import com.strizhonovapps.lexixapp.service.ImageService
import com.strizhonovapps.lexixapp.service.LanguageService
import com.strizhonovapps.lexixapp.service.LanguageServiceImpl
import com.strizhonovapps.lexixapp.service.UnsplashApiProvider
import com.strizhonovapps.lexixapp.service.WordService
import com.strizhonovapps.lexixapp.service.WordServiceImpl
import com.strizhonovapps.lexixapp.util.PrefsDecorator
import com.strizhonovapps.lexixapp.util.UrlAudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class BeanModule {

    @Provides
    fun provideWordService(service: WordServiceImpl): WordService = service

    @Provides
    fun provideWordSuggestionDao(dao: WordSuggestionDaoImpl): WordSuggestionDao = dao

    @Provides
    fun provideWordDao(dao: WordDaoImpl): WordDao = dao

    @Provides
    fun provideLanguageService(service: LanguageServiceImpl): LanguageService = service

    @Provides
    fun providePreferences(@ApplicationContext context: Context): PrefsDecorator =
        PrefsDecorator(PreferenceManager.getDefaultSharedPreferences(context))

    @Provides
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager = context.assets

    @Provides
    fun provideInputMethodManager(@ApplicationContext context: Context): InputMethodManager =
        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

    @Provides
    fun provideUrlAudioPlayer(): UrlAudioPlayer = UrlAudioPlayer()

    @Provides
    fun provideImageService(
        @ApplicationContext context: Context,
        unsplashApiProvider: UnsplashApiProvider
    ): ImageService =
        ImageService(ContextWrapper(context), unsplashApiProvider)

    @Provides
    @Named("yndxDictionaryApiKey")
    fun provideYndxDictApiKey(@ApplicationContext context: Context): String =
        context.getString(R.string.yandex_dictionary_api_key)

    @Provides
    @Named("unsplashApiKey")
    fun provideUnsplashApiKey(@ApplicationContext context: Context): String =
        context.getString(R.string.unsplash_api_key)

}
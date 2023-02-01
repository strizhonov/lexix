package com.strizhonovapps.lexixapp.service

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.strizhonovapps.lexixapp.model.Word
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class ImageService(
    private val contextWrapper: ContextWrapper,
    private val unsplashApiProvider: UnsplashApiProvider
) {

    private val imgRelativeDir = "lexix_img"
    private val imgFormat = Bitmap.CompressFormat.JPEG

    fun saveImage(id: Long, img: Bitmap) {
        val imgDirectory = contextWrapper.getDir(imgRelativeDir, Context.MODE_PRIVATE)
        val imgFile = File(imgDirectory, "$id.${imgFormat.toString().lowercase()}")
        FileOutputStream(imgFile).use { fileOutputStream ->
            img.compress(imgFormat, 100, fileOutputStream)
        }
    }

    suspend fun downloadImage(keyWord: String) = unsplashApiProvider.getImage(keyWord)

    fun getSquaredImage(word: Word): Bitmap? =
        this.getImage(word.id)
            ?.let { bitmap ->
                Bitmap.createBitmap(
                    bitmap,
                    if (bitmap.width >= bitmap.height) bitmap.width / 2 - bitmap.height / 2 else 0,
                    if (bitmap.height >= bitmap.width) bitmap.height / 2 - bitmap.width / 2 else 0,
                    minOf(bitmap.width, bitmap.height),
                    minOf(bitmap.width, bitmap.height)
                )
            }

    private fun getImage(id: Long) =
        try {
            val imgDirectory = contextWrapper.getDir(imgRelativeDir, Context.MODE_PRIVATE)
            val imgFile = File(imgDirectory, "$id.${imgFormat.toString().lowercase()}")
            FileInputStream(imgFile).use { fileInputStream ->
                BitmapFactory.decodeStream(fileInputStream)
            }
        } catch (e: FileNotFoundException) {
            null
        }
}
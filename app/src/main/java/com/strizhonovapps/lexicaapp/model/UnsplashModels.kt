package com.strizhonovapps.lexicaapp.model

data class UnsplashRootModel(var results: List<UnsplashImgs> = emptyList())

data class UnsplashImgs(var urls: UnsplashImgUrls?)

data class UnsplashImgUrls(
    var raw: String?,
    var full: String?,
    var regular: String?,
    var small: String?,
    var thumb: String?,
)
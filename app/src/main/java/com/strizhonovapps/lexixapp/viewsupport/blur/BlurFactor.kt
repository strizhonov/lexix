package com.strizhonovapps.lexixapp.viewsupport.blur

import android.graphics.Color

data class BlurFactor(
    var width: Int = 0,
    var height: Int = 0,
    var radius: Int = 4,
    var sampling: Int = 6,
    var color: Int = Color.TRANSPARENT,
)
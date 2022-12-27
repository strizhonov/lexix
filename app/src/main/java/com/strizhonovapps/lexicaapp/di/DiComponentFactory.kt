package com.strizhonovapps.lexicaapp.di

import android.content.Context

object DiComponentFactory {

    private var diComponent: DiComponent? = null

    fun initIfNecessary(context: Context) {
        if (diComponent == null) {
            diComponent = DaggerDiComponent.builder()
                .beanModule(BeanModule(context))
                .build()
        }
    }

    fun getInstance() =
        diComponent ?: throw IllegalAccessException("DiComponent wasn't initialized")

}


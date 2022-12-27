package com.strizhonovapps.lexicaapp.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class LearningLanguageKtTest {

    @Test
    fun `should throw RuntimeException if out of bounds index passed`() {
        assertThrows<RuntimeException> {
            getByIndex(SupportedLanguage.values().size + 1000)
        }
    }

}